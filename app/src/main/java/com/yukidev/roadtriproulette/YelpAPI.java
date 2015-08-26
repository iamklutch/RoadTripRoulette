package com.yukidev.roadtriproulette;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Random;

/**
 * Code sample for accessing the Yelp API V2.
 *
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 *
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 *
 */

/**
 * Created by YukiDev on 7/24/2015.
 */
public class YelpAPI{

    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "dinner";
    private static final String DEFAULT_LOCATION = "San Francisco, CA";
    private static final String DEFAULT_LATLONG = "41.2153, -111.97";
    private static final int SEARCH_LIMIT = 20;
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";
    private static Boolean mError = false;

    /*
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    private static final String CONSUMER_KEY = "rQ_ZJCs_hiFEanook8C4Yg";
    private static final String CONSUMER_SECRET = "gJjdThNh4rI1p2bT-izNV-BPyws";
    private static final String TOKEN = "nRO4PNuOQ4GpsbUFNG7FifdqExsOJ6CD";
    private static final String TOKEN_SECRET = "HXCqjmPCa334sO64znA7AaFxaRw";

    Context mContext;
    OAuthService service;
    Token accessToken;

    /**
     * Setup the Yelp API OAuth credentials.


     @Parameter consumerKey = {"rQ_ZJCs_hiFEanook8C4Yg"})
     @Parameter consumerSecret gJjdThNh4rI1p2bT-izNV-BPyws
     @Parameter token nRO4PNuOQ4GpsbUFNG7FifdqExsOJ6CD
     @Parameter tokenSecret HXCqjmPCa334sO64znA7AaFxaRw
     */

    public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret, Context context) {

        mContext = context;

        this.service =
                new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
                        .apiSecret(consumerSecret).build();
        this.accessToken = new Token(token, tokenSecret);


    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(String term, String location) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
//       request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("ll", location);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        System.out.println("Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param yelpApi <tt>YelpAPI</tt> service instance
     * @param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
     */
    private static void queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli, final Context context) {
        String searchResponseJSON =
                yelpApi.searchForBusinessesByLocation(yelpApiCli.term, yelpApiCli.latlong);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            Log.d("YelpAPI: ", "Error: could not parse JSON response:" + searchResponseJSON);
            System.exit(1);
        }

        try {
            JSONObject error = (JSONObject) response.get("error");
            JSONArray businesscheck = (JSONArray) response.get("businesses");

            // if there are no businesses found
            if (response.size() <= 1 || businesscheck.isEmpty()) {
                mError = true;

                Handler errorHandler = new Handler(context.getMainLooper());
                Runnable errorIntentRunnable = new Runnable() {

                    @Override
                    public void run() {
                        final Intent errorIntent = new Intent(context, MainActivity.class);
                        errorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        errorIntent.putExtra("error", "noBusiness");
                        context.startActivity(errorIntent);

                    }
                };
                errorHandler.post(errorIntentRunnable);

            }

            // if Yelp isn't available here yet. . .
            if (error.get("text").equals("API unavailable in this location")){
                mError = true;

                Handler errorHandler = new Handler(context.getMainLooper());
                Runnable errorIntentRunnable = new Runnable() {

                    @Override
                    public void run() {
                        final Intent errorIntent = new Intent(context, MainActivity.class);
                        errorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        errorIntent.putExtra("error", "noYelp");
                        context.startActivity(errorIntent);

                    }
                };
                errorHandler.post(errorIntentRunnable);
            }
        }catch (NullPointerException npe) {
            // do nothing
        }

        if (!mError == true) {

            JSONArray businesses = (JSONArray) response.get("businesses");
            Random random = new Random();
            int i = random.nextInt(businesses.size());
            JSONObject randomBusiness = (JSONObject) businesses.get(i);
            String businessName = randomBusiness.get("name").toString();

            String businessImgUrl;
            try {
                businessImgUrl = randomBusiness.get("image_url").toString();
            } catch (NullPointerException e) {
                businessImgUrl = "NA";
            }

            double rating = Double.parseDouble(randomBusiness.get("rating").toString());
            String ratingUrl = randomBusiness.get("rating_img_url_large").toString();
            String yelpBusinessUrl = randomBusiness.get("url").toString();
            // get direction, coordinates and city
            JSONObject location = (JSONObject) randomBusiness.get("location");
            JSONObject coordinates = (JSONObject) location.get("coordinate");
            String businessCity = location.get("city").toString();
            double businessLat = Double.parseDouble(coordinates.get("latitude").toString());
            double businessLng = Double.parseDouble(coordinates.get("longitude").toString());

            Intent intent = new Intent(context, ResultActivity.class);
            intent.putExtra("lat", businessLat);
            intent.putExtra("lng", businessLng);
            intent.putExtra("name", businessName);
            intent.putExtra("city", businessCity);
            intent.putExtra("imageUrl", businessImgUrl);
            intent.putExtra("rating", rating);
            intent.putExtra("ratingUrl", ratingUrl);
            intent.putExtra("yelpUrl", yelpBusinessUrl);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Command-line interface for the sample Yelp API runner.
     */
    private static class YelpAPICLI {
        @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
        public String term = DEFAULT_TERM;

        @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
        public String location = DEFAULT_LOCATION;

        @Parameter(names = {"-L", "--ll"}, description = "Location Lat Long")
        public String latlong = DEFAULT_LATLONG;
    }

    /**
     * Main entry for sample Yelp API requests.
     * <p>
     * After entering your OAuth credentials, execute <tt><b>run.sh</b></tt> to run this example.
     */
    public static void main(String[] args, Context context) {
        YelpAPICLI yelpApiCli = new YelpAPICLI();
        new JCommander(yelpApiCli, args);

        YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET, context);
        queryAPI(yelpApi, yelpApiCli, context);
    }
}