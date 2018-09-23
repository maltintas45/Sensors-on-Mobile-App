package com.maltintas.zer.sm;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    /* we will collect
        accelerometer
        gyroscope
        light sersor
        proximity sersor
        gps
     */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensordata";
        public static  final String TIMESTAMP="timestamp";
        public static final String ACCELEROMETER_X = "accx";
        public static final String ACCELEROMETER_Y = "accy";
        public static final String ACCELEROMETER_Z = "accz";
        public static final String GYROSCOPE_X = "gyrx";
        public static final String GYROSCOPE_Y = "gyry";
        public static final String GYROSCOPE_Z = "gyrz";
        public static final String LIGHT = "light";
        public static final String PROXIMITY = "proximity";
        public static final String GPS_LONGITUDE="gpslongitude";
        public static final String GPS_LATITUDE="gpslatitude";
    }
    public static class AppSet implements BaseColumns{
        public static final String TABLE_NAME="appdata";
        public static final String DEVICE_ID="device_id";
        public static final String USER_NICK="user_nick";
        public static final String USER_GENDER="user_gender";
        public static final String EXPLANATION="data_explanation";
    }
}