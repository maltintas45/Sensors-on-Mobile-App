package com.maltintas.zer.sm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.maltintas.zer.sm.FeedReaderContract.FeedEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class SmService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    Sensor mAccelerometer, mGyroscope, mLight, mProximity;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FeedReaderDbHelper dh;

    private UploadDataTask up;

    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    double accx = 0, accy = 0, accz = 0, gyrx = 0, gyry = 0, gyrz = 0, light = 0, proximity = 0;

    public SmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int starId) {
        // show someting to tell that the service started
        Toast.makeText( this, getResources().getString( R.string.thanks_contribution ), Toast.LENGTH_LONG ).show();
        // get sersors
        mSensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );
        mAccelerometer = mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        mGyroscope = mSensorManager.getDefaultSensor( Sensor.TYPE_GYROSCOPE );
        mLight = mSensorManager.getDefaultSensor( Sensor.TYPE_LIGHT );
        mProximity = mSensorManager.getDefaultSensor( Sensor.TYPE_PROXIMITY );

        mSensorManager.registerListener( this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener( this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener( this, mLight, SensorManager.SENSOR_DELAY_NORMAL );
        mSensorManager.registerListener( this, mProximity, SensorManager.SENSOR_DELAY_NORMAL );


        mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if (!mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) && mLocationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER )) {
            Toast.makeText( this, getResources().getString( R.string.location_provider_is_not_avaible ), Toast.LENGTH_LONG ).show();
            showAlert();
        } else {
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitudeGPS = location.getLongitude();
                    latitudeGPS = location.getLatitude();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {


                }
            };
            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                longitudeGPS=0;
                latitudeGPS=0;
            }
            else {
                Location location=mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                if (location!=null) {
                    longitudeGPS = location.getLongitude();
                    latitudeGPS = location.getLatitude();
                }

                mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener );
            }
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        // active listen to user logged in or not.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Firebase", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Firebase", "onAuthStateChanged:signed_out");
                }

            }
        };
        mAuth.addAuthStateListener( mAuthListener );


        return START_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the sensor values
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accx=event.values[0];
            accy=event.values[1];
            accz=event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyrx=event.values[0];
            gyry=event.values[1];
            gyrz=event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            light=event.values[0];
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximity=event.values[0];
        }
        //save to SQLite database
        long timestamp= System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put( FeedEntry.TIMESTAMP, String.valueOf(timestamp) );
        values.put(FeedEntry.ACCELEROMETER_X,Double.toString(accx));
        values.put(FeedEntry.ACCELEROMETER_Y,Double.toString(accy));
        values.put( FeedEntry.ACCELEROMETER_Z,Double.toString(accz ));
        values.put(FeedEntry.GYROSCOPE_X,Double.toString((gyrx)));
        values.put( FeedEntry.GYROSCOPE_Y,Double.toString(gyry ));
        values.put( FeedEntry.GYROSCOPE_Z,Double.toString(gyrz ));
        values.put(FeedEntry.LIGHT,Double.toString(light));
        values.put( FeedEntry.PROXIMITY,Double.toString(proximity));
        values.put( FeedEntry.GPS_LONGITUDE,Double.toString( longitudeGPS ) );
        values.put( FeedEntry.GPS_LATITUDE,Double.toString( latitudeGPS ) );

        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try
        {
            // Insert the new row, returning the primary key value of the new row
            int h= (int) db.insert(FeedEntry.TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.toString());
        }
        db.close();
        if(isUploadRequired() & isNetworkConvenient( getBaseContext())) {
            up=new UploadDataTask();
            if (up.getStatus()!=AsyncTask.Status.RUNNING) {
                up.execute( 0 );
                Toast.makeText( this, "Uploading files..", Toast.LENGTH_SHORT ).show();
            }
        }

        //Toast.makeText(this, longitudeGPS+" "+latitudeGPS, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean isUploadRequired()
    {
        // If the db size over 10 MB uploading is required
        File file = new File("/data/data/com.maltintas.zer.sm/databases/"+FeedReaderDbHelper.DATABASE_NAME);
        int file_size = Integer.parseInt(String.valueOf(file.length()/1024));

        if (file_size>10000)
            return true;
        else
            return false;
    }
    public boolean isNetworkConvenient(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        boolean isWifi=activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        return isConnected & isWifi;
    }

    public  void  onDestroy(){
        Toast.makeText(this, getResources().getString(R.string.glad_to), Toast.LENGTH_LONG).show();
        mSensorManager.unregisterListener(this);

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }
    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public int exportSqliteTableToCsv(String query,String filename)
    {

        FeedReaderDbHelper dh;
        int row_id=0;
        try {
            StringBuilder sbRows=new StringBuilder();
            List<String> rowItems=new ArrayList<String>();
            dh=new FeedReaderDbHelper(getBaseContext());
            SQLiteDatabase db= dh.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            int count = cursor.getCount();

            if (count>0)
                while(cursor.moveToNext()) {
                row_id=cursor.getInt( 0 );
                    for(int i=1;i<cursor.getColumnCount();i++)
                        rowItems.add( cursor.getString(i));
                    sbRows.append( TextUtils.join(", ",rowItems)+"\n");
                    rowItems.clear();
                }
            cursor.close();
            writeToFile( sbRows.toString(),filename,getBaseContext());
            deleteSendedRows( row_id );
            return row_id;
        }
        catch (Exception e) {
            return row_id;
        }

    }
    public void writeToFile(String data,String filename,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public void deleteSendedRows(int rowId)
    {
        dh=new FeedReaderDbHelper(getBaseContext());
        SQLiteDatabase db= dh.getReadableDatabase();
        try {
            db.delete(FeedEntry.TABLE_NAME, "_ID<="+String.valueOf( rowId ), null);
            db.close();
        }
        catch (Exception e)
        {
            Log.e("Exception","Delete exception: "+e.toString());
        }
    }
    public void uploadFileFireBase(Uri file, StorageReference storageRef, final int biggestSendedID) {
        if (file != null) {

            StorageReference riversRef = storageRef.child( "sm/"+createTitle() );

            riversRef.putFile( file )
                    .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //deleteSendedRows(biggestSendedID);
                        }
                    } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    } )
                    .addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    } );
        }
    }
    class UploadDataTask extends AsyncTask<Integer, Integer, String> {
        Uri file = Uri.fromFile( new File( "/data/data/com.maltintas.zer.sm/files/yalp" ) );
        String query="Select * from "+ FeedReaderContract.FeedEntry.TABLE_NAME;
        private int biggestSendedID=0;

        boolean data_is_ready=false;
        @Override
        protected String doInBackground(Integer... params) {
            if (isInternetAvailable() &&!data_is_ready) {
                biggestSendedID=exportSqliteTableToCsv(query,"yalp");
                data_is_ready=true;
            }
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            if (data_is_ready)
                uploadFileFireBase( file, mStorageRef,biggestSendedID);
        }

        @Override
        protected void onPreExecute() {
        }
        @Override
        protected void onProgressUpdate(Integer... values) {

        }
    }
    public String createTitle()
    {
        String title="";
        SQLiteDatabase db= dh.getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+FeedReaderContract.AppSet.TABLE_NAME+" WHERE _id=1", null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            title+=cursor.getString( 4 );
            String gender=cursor.getString( 2 );
            if(gender=="male")
                title+="_M";
            else
                title+="_F";
            title+="_"+cursor.getString( 1 );
            title+="_"+String.valueOf(System.currentTimeMillis());
            title+="("+cursor.getString( 3 )+")";
        }
        cursor.close();
        return title;
    }
}
