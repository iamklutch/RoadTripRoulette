package com.yukidev.roadtriproulette;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by James on 8/14/2015.
 */
public class Cities implements Parcelable{

    private String mName;
    private double mLat;
    private double mLng;
    private double mDistance;


    public String getName() { return mName;}

    public void setName(String name) {mName = name;}

    public double getLat() {return mLat;}

    public void setLat(double lat) {this.mLat = lat;}

    public double getLng() {return mLng;}

    public void setLng(double lng) {this.mLng = lng;}

    public double getDistance() {return mDistance;}

    public void setDistance(double distance) {this.mDistance = distance;}

    private Cities(Parcel in) {

        mName = in.readString();
        mLat = in.readDouble();
        mLng = in.readDouble();
        mDistance = in.readDouble();
    }

    public Cities(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mName);
        dest.writeDouble(mLat);
        dest.writeDouble(mLng);
        dest.writeDouble(mDistance);
    }
    public static final Creator<Cities> CREATOR = new Creator<Cities>() {
        @Override
        public Cities createFromParcel(Parcel source) {return new Cities(source);}

        @Override
        public Cities[] newArray(int size) {return new Cities[size];}
    };
}
