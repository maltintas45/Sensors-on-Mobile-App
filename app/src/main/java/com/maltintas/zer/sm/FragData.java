package com.maltintas.zer.sm;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.maltintas.zer.sm.FeedReaderContract.FeedEntry;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragData extends Fragment {

    public FragData() {
        // Required empty public constructor
    }


    private TableLayout mTableLayout;
    private ProgressDialog mProgressBar;
    private SQLiteDatabase db;
    private FeedReaderDbHelper mDbHelper;
    private Context context;
    int textSize,smallTextSize,mediumTextSize;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_data,container,false);
        mDbHelper = new FeedReaderDbHelper(getContext());
        context=getContext();

        mProgressBar = new ProgressDialog(getContext());

        // setup the table
        mTableLayout = (TableLayout) view.findViewById(R.id.tableSensors);

        mTableLayout.setStretchAllColumns(true);
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {

            try {
                loadData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
                Log.d("MyFragment", "Not visible anymore.  Stopping audio.");
                // TODO stop audio playback
            }
        }
    }

    public Cursor getSensorDataFromSQLiteDatabase()
    {
        db= mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedEntry.TIMESTAMP,
                FeedEntry.ACCELEROMETER_X,
                FeedEntry.ACCELEROMETER_Y,
                FeedEntry.ACCELEROMETER_Z,
                FeedEntry.GYROSCOPE_X,
                FeedEntry.GYROSCOPE_Y,
                FeedEntry.GYROSCOPE_Z,
                FeedEntry.LIGHT,
                FeedEntry.PROXIMITY,
                FeedEntry.GPS_LONGITUDE,
                FeedEntry.GPS_LATITUDE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = FeedEntry.LIGHT + " = *";
        String[] selectionArgs = { "*" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedEntry._ID+ " DESC";

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,  // The table to query
                null,            // The array of columns to return (pass null to get all)
                null,          // The columns for the WHERE clause
                null,       // The values for the WHERE clause
                null,           // don't group the rows
                null,             // don't filter by row groups
                sortOrder,               // The sort order
                "20"
        );
        return cursor;
    }

    public void loadData() throws ParseException {

        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        int textSize = 0, smallTextSize =0, mediumTextSize = 0;


        textSize = 10;
        smallTextSize = 12;
        mediumTextSize = 20;

        Cursor cursor=getSensorDataFromSQLiteDatabase();

        FeedEntry sensorsRow =new FeedEntry();

        SensorData[] data= new SensorData[cursor.getCount()];

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        int k=0;
        long previousTimestamp=0;
        while(cursor.moveToNext()) {
            SensorData d=new SensorData();
            d.id=cursor.getInt(cursor.getColumnIndexOrThrow(FeedEntry._ID));
            d.timestamp=cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.TIMESTAMP));
            d.timeinterval=Long.valueOf(d.timestamp).longValue()-previousTimestamp;
            d.accx=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.ACCELEROMETER_X ));
            d.accy=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.ACCELEROMETER_Y ));
            d.accz=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.ACCELEROMETER_Z ));
            d.gyrx=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.GYROSCOPE_X ));
            d.gyry=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.GYROSCOPE_Y ));
            d.gyrz=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.GYROSCOPE_Z ));
            d.light=cursor.getString(cursor.getColumnIndexOrThrow( FeedEntry.LIGHT ));
            d.proximity=cursor.getString(cursor.getColumnIndexOrThrow( FeedEntry.PROXIMITY ));
            d.gpslonglitude=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.GPS_LONGITUDE ) );
            d.gpslatitude=cursor.getString( cursor.getColumnIndexOrThrow( FeedEntry.GPS_LATITUDE ) );
            previousTimestamp=Long.valueOf(d.timestamp).longValue();
            data[k]=d;
            k++;
        }
        cursor.close();
        db.close();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        int rows = data.length;
        //getSupportActionBar().setTitle("Invoices (" + String.valueOf(rows) + ")");
        TextView textSpacer = null;

        mTableLayout.removeAllViews();

        // -1 means heading row
        for(int i = -1; i < rows; i ++) {
            SensorData row = null;
            if (i > -1)
                row = data[i];
            else {
                textSpacer = new TextView(context);
                textSpacer.setText("MMM");

            }
            // data columns

            final LinearLayout layID = new LinearLayout(context);
            layID.setOrientation(LinearLayout.VERTICAL);
            layID.setPadding(0, 10, 0, 10);
            layID.setBackgroundColor(Color.parseColor("#f8f8f8"));

            final TextView tv0 = new TextView(context);
            if (i == -1) {
                tv0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv0.setPadding(5, 5, 0, 5);
                tv0.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv0.setPadding(5, 0, 0, 5);
                tv0.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            tv0.setGravity(Gravity.TOP);

            if (i == -1) {
                tv0.setText("ID");
                tv0.setBackgroundColor(Color.parseColor("#f0f0f0"));
            } else {
                tv0.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv0.setTextColor(Color.parseColor("#000000"));
                tv0.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
                tv0.setText(String.valueOf(row.id));
            }
            layID.addView(tv0);
            if (i > -1) {
                final TextView tv0b = new TextView(context);
                tv0b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv0b.setGravity(Gravity.RIGHT);
                tv0b.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv0b.setPadding(5, 1, 0, 5);
                tv0b.setTextColor(Color.parseColor("#aaaaaa"));
                tv0b.setBackgroundColor(Color.parseColor("#f8f8f8"));
                String interval= String.valueOf(row.timeinterval);
                //SimpleDateFormat sf = new SimpleDateFormat("MMddyyHHmmss");
                //interval=((interval.length() >= 10) ? new Date( Long.parseLong(interval)).toString() : interval);
                tv0b.setText(interval);
                layID.addView( tv0b );
            }

            final TextView tv2 = new TextView(context);
            if (i == -1) {
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            tv2.setGravity(Gravity.LEFT);

            tv2.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv2.setText("Light");
                tv2.setBackgroundColor(Color.parseColor("#f7f7f7"));
            }else {
                tv2.setBackgroundColor(Color.parseColor("#ffffff"));
                tv2.setTextColor(Color.parseColor("#000000"));
                tv2.setText(String.valueOf(row.light));
            }

            final LinearLayout layGPS = new LinearLayout(context);
            layGPS.setOrientation(LinearLayout.VERTICAL);
            layGPS.setPadding(0, 10, 0, 10);
            layGPS.setBackgroundColor(Color.parseColor("#f8f8f8"));
            final TextView tv6 = new TextView(context);
            if (i == -1) {
                tv6.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv6.setPadding(5, 5, 0, 5);
                tv6.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv6.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv6.setPadding(5, 0, 0, 5);
                tv6.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            tv6.setGravity(Gravity.TOP);

            if (i == -1) {
                tv6.setText("GPS");
                tv6.setBackgroundColor(Color.parseColor("#f0f0f0"));
            } else {
                tv6.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv6.setTextColor(Color.parseColor("#000000"));
                tv6.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
                tv6.setText(String.valueOf(row.gpslonglitude));
            }
            layGPS.addView(tv6);
            if (i > -1) {
                final TextView tv6b = new TextView(context);
                tv6b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv6b.setGravity(Gravity.RIGHT);
                tv6b.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv6b.setPadding(5, 1, 0, 5);
                tv6b.setTextColor(Color.parseColor("#aaaaaa"));
                tv6b.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv6b.setText(String.valueOf(row.gpslatitude));
                layGPS.addView( tv6b );
            }


            final LinearLayout layAcc = new LinearLayout(context);
            layAcc.setOrientation(LinearLayout.VERTICAL);
            layAcc.setPadding(0, 10, 0, 10);
            layAcc.setBackgroundColor(Color.parseColor("#f8f8f8"));

            final TextView tv3 = new TextView(context);
            if (i == -1) {
                tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv3.setPadding(5, 5, 0, 5);
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv3.setPadding(5, 0, 0, 5);
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            tv3.setGravity(Gravity.TOP);

            if (i == -1) {
                tv3.setText("Acc");
                tv3.setBackgroundColor(Color.parseColor("#f0f0f0"));
            } else {
                tv3.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv3.setTextColor(Color.parseColor("#000000"));
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
                tv3.setText(String.valueOf(row.accx));
            }
            layAcc.addView(tv3);


            if (i > -1) {
                final TextView tv3b = new TextView(context);
                tv3b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv3b.setGravity(Gravity.RIGHT);
                tv3b.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv3b.setPadding(5, 1, 0, 5);
                tv3b.setTextColor(Color.parseColor("#aaaaaa"));
                tv3b.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv3b.setText(String.valueOf(row.accx));

                final TextView tv3a = new TextView(context);
                tv3a.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv3a.setGravity(Gravity.RIGHT);
                tv3a.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv3a.setPadding(5, 1, 0, 5);
                tv3a.setTextColor(Color.parseColor("#aaaaaa"));
                tv3a.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv3a.setText(String.valueOf(row.accy));
                layAcc.addView(tv3a);

                final TextView tv3c = new TextView(context);
                tv3c.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv3c.setGravity(Gravity.RIGHT);
                tv3c.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv3c.setPadding(5, 1, 0, 5);
                tv3c.setTextColor(Color.parseColor("#aaaaaa"));
                tv3c.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv3c.setText(String.valueOf(row.accz));
                layAcc.addView( tv3c );
            }

            final LinearLayout layGyr = new LinearLayout(context);
            layGyr.setOrientation(LinearLayout.VERTICAL);
            layGyr.setGravity(Gravity.RIGHT);
            layGyr.setPadding(0, 10, 0, 10);
            layGyr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));



            final TextView tv4 = new TextView(context);
            if (i == -1) {
                tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv4.setPadding(5, 5, 1, 5);
                layGyr.setBackgroundColor(Color.parseColor("#f7f7f7"));
            } else {
                tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tv4.setPadding(5, 0, 1, 5);
                layGyr.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            tv4.setGravity(Gravity.LEFT);

            if (i == -1) {
                tv4.setText("Gyro");
                tv4.setBackgroundColor(Color.parseColor("#f7f7f7"));
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv4.setBackgroundColor(Color.parseColor("#ffffff"));
                tv4.setTextColor(Color.parseColor("#000000"));
                tv4.setText(String.valueOf(row.gyrx));
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            layGyr.addView(tv4);


            if (i > -1) {
                final TextView tv4b = new TextView(context);
                tv4b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv4b.setGravity(Gravity.LEFT);
                tv4b.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv4b.setPadding(2, 2, 1, 5);
                tv4b.setTextColor(Color.parseColor("#000000"));
                tv4b.setBackgroundColor(Color.parseColor("#ffffff"));
                tv4b.setText(String.valueOf( row.gyry ));
                layGyr.addView(tv4b);

                final TextView tv4a = new TextView(context);
                tv4a.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                tv4a.setGravity(Gravity.LEFT);
                tv4a.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv4a.setPadding(2, 2, 1, 5);
                tv4a.setTextColor(Color.parseColor("#000000"));
                tv4a.setBackgroundColor(Color.parseColor("#ffffff"));
                tv4a.setText(String.valueOf( row.gyrz ));
                layGyr.addView(tv4a);
            }

            final TextView tv5 = new TextView(context);
            if (i == -1) {
                tv5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }
            tv5.setGravity(Gravity.LEFT);

            tv5.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv5.setText("Proximity");
                tv5.setBackgroundColor(Color.parseColor("#f7f7f7"));
            }else {
                tv5.setBackgroundColor(Color.parseColor("#ffffff"));
                tv5.setTextColor(Color.parseColor("#000000"));
                tv5.setText(String.valueOf( row.proximity));
            }


            // add table row
            final TableRow tr = new TableRow(context);
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);



            tr.addView(layID);
            tr.addView(layGyr);
            tr.addView(layAcc);
            tr.addView(tv2);
            tr.addView(tv5);
            tr.addView(layGPS);

            if (i > -1) {

                tr.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        TableRow tr = (TableRow) v;
                        //do whatever action is needed

                    }
                });


            }
            mTableLayout.addView(tr, trParams);

            if (i > -1) {

                // add separator row
                final TableRow trSep = new TableRow(context);
                TableLayout.LayoutParams trParamsSep = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);

                trSep.setLayoutParams(trParamsSep);
                TextView tvSep = new TextView(context);
                TableRow.LayoutParams tvSepLay = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                tvSepLay.span = 4;
                tvSep.setLayoutParams(tvSepLay);
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tvSep.setHeight(1);

                trSep.addView(tvSep);
                mTableLayout.addView(trSep, trParamsSep);
            }


        }
    }

    public class SensorData {

        public int id;
        public String timestamp;
        public long timeinterval;
        public String accx;
        public String accy;
        public String accz;
        public String gyrx;
        public String gyry;
        public String gyrz;
        public String light;
        public String proximity;
        public String gpslonglitude;
        public String gpslatitude;
    }
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService( Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
