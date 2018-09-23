package com.maltintas.zer.sm;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class FragSettings extends Fragment implements onReq {
    public FragSettings() throws FileNotFoundException {
    }
    private StorageReference mStorageRef;
    private FeedReaderDbHelper dh;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_setting,container,false);

        Switch contribute_switch = (Switch) view.findViewById(R.id.switch1);

        if (isServiceRunning( SmService.class ))
            contribute_switch.setChecked( true );
        else
            contribute_switch.setChecked( false );

        contribute_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do something when checked
                if (isChecked) {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED) {
                        requestPermissions( new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},707);
                         return;
                        }
                    }
                    else
                       getActivity().startService(new Intent( getActivity().getBaseContext(), SmService.class ));

                }
                else {
                    //do something when unchecked
                    getActivity().stopService(new Intent(getActivity().getBaseContext(),SmService.class));
                }
            }
        });


        Switch auto_sending_switch = (Switch) view.findViewById(R.id.switch2);
        auto_sending_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //do something when checked
                    Toast.makeText(getActivity(), getResources().getString(R.string.auto_send_opened), Toast.LENGTH_LONG).show();
                } else {
                    //do something when unchecked
                    Toast.makeText(getActivity(), getResources().getString(R.string.auto_send_closed), Toast.LENGTH_LONG).show();
                }
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button sending_button=(Button)view.findViewById( R.id.senddata);
        sending_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new uploadDataTask().execute( 0 );
            }
        });

        final EditText nickName = (EditText) view.findViewById(R.id.nickName);
        final EditText explanationData=(EditText)view.findViewById( R.id.explanation );
        final RadioButton genderMale=(RadioButton)view.findViewById( R.id.radioM );
        RadioGroup radioGroup=(RadioGroup)view.findViewById( R.id.radioGrpGender );
        RadioButton genderFemale=(RadioButton)view.findViewById( R.id.radioF );
        dh=new FeedReaderDbHelper(getContext());
        SQLiteDatabase db= dh.getReadableDatabase();
        boolean updaterecord=false;
        String device_id="";
        try
        {
            Cursor cursor=db.rawQuery("SELECT * FROM "+FeedReaderContract.AppSet.TABLE_NAME+" WHERE _id=1", null);
            cursor.moveToFirst();
            if(cursor.getCount()>0) {
                updaterecord=true;
                nickName.setText( cursor.getString( 1 ) );
                explanationData.setText( cursor.getString( 3 ) );
                String gender=cursor.getString( 2 );
                if(gender.equals("male"))
                    genderMale.setChecked( true );
                else
                    genderFemale.setChecked( true );
                device_id=cursor.getString( 4 );
            }
            cursor.close();
        }
        catch (Exception e)
        {
            Log.e( "SQLite:",e.getMessage() );
        }

        final boolean update=updaterecord;
        final String device=device_id;

        nickName.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                SQLiteDatabase db= dh.getReadableDatabase();
                if (!hasFocus) {
                    // code to execute when EditText loses focus

                    if(!update) {// row Not Exist

                        db.insert(FeedReaderContract.AppSet.TABLE_NAME, null,getValues(genderMale,nickName,explanationData,device));

                    }
                    else {
                        String[] args= new String[]{"1"};
                        db.update( FeedReaderContract.AppSet.TABLE_NAME, getValues(genderMale,nickName,explanationData,device), "_ID=?",args);
                    }
                }
            }
        });
        explanationData.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                SQLiteDatabase db= dh.getReadableDatabase();
                if (!hasFocus) {
                    // code to execute when EditText loses focus

                    if(!update) {// row Not Exist

                        db.insert(FeedReaderContract.AppSet.TABLE_NAME, null,getValues(genderMale,nickName,explanationData,device));

                    }
                    else {
                        String[] args= new String[]{"1"};
                        db.update( FeedReaderContract.AppSet.TABLE_NAME, getValues(genderMale,nickName,explanationData,device), "_ID=?",args);
                    }
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                SQLiteDatabase db= dh.getReadableDatabase();
                if(!update) {// row Not Exist

                    db.insert(FeedReaderContract.AppSet.TABLE_NAME, null,getValues(genderMale,nickName,explanationData,device));

                }
                else {
                    String[] args= new String[]{"1"};
                    db.update( FeedReaderContract.AppSet.TABLE_NAME, getValues(genderMale,nickName,explanationData,device), "_ID=?",args);
                }
            }
        });

        return view;
    }
    public ContentValues getValues(RadioButton genderMale,EditText nickName, EditText explanationData,String device_id)
    {
        final ContentValues values = new ContentValues();
        values.put( FeedReaderContract.AppSet.USER_NICK,nickName.getText().toString());
        values.put( FeedReaderContract.AppSet.EXPLANATION,explanationData.getText().toString());
        if (genderMale.isChecked())
            values.put( FeedReaderContract.AppSet.USER_GENDER,"male");
        else
            values.put( FeedReaderContract.AppSet.USER_GENDER,"female" );
        values.put( FeedReaderContract.AppSet.DEVICE_ID,device_id);
        return  values;
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
    public void uploadFileFireBase(Uri file, StorageReference storageRef, final int biggestSendedId)
    {
        if(file!=null) {
            final ProgressDialog progressDialog=new ProgressDialog(getContext());
            progressDialog.setTitle( "" );
            progressDialog.show();

            StorageReference riversRef = storageRef.child( "sm/"+createTitle());

            riversRef.putFile( file )
                    .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            deleteSendedRows( biggestSendedId );
                            Toast.makeText( getActivity(), "File uploaded", Toast.LENGTH_SHORT ).show();
                        }
                    } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            progressDialog.dismiss();
                            Toast.makeText( getActivity(),  exception.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    } )
                    .addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot){
                            double progress=(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage( (int)progress+"% uploaded.." );
                        }
                    } )  ;
        }
        else {
            // display db file does not exist
            Toast.makeText( getActivity(), getResources().getString( R.string.db_does_not_exist ), Toast.LENGTH_SHORT ).show();
        }
    }
    public void deleteSendedRows(int rowId)
    {
        dh=new FeedReaderDbHelper(getContext());
        SQLiteDatabase db= dh.getReadableDatabase();
        try {
            db.delete( FeedReaderContract.FeedEntry.TABLE_NAME, "_ID<="+String.valueOf( rowId ), null);
            db.close();
        }
        catch (Exception e)
        {
            Log.e("Exception","Delete exception: "+e.toString());
        }
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
            dh=new FeedReaderDbHelper(getContext());
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
            writeToFile( sbRows.toString(),filename,getContext());
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
    class uploadDataTask extends AsyncTask<Integer, Integer, String> {
        Uri file = Uri.fromFile( new File( "/data/data/com.maltintas.zer.sm/files/yalp" ) );
        String query="Select * from "+ FeedReaderContract.FeedEntry.TABLE_NAME;
        ProgressDialog ringProgressDialog = ProgressDialog.show(getContext(), "",	"Data preparing ...", true);
        boolean data_is_ready=false;
        private int biggestSendedID=0;
        @Override
        protected String doInBackground(Integer... params) {
            if (isInternetAvailable()) {
                biggestSendedID=exportSqliteTableToCsv(query,"yalp");
                data_is_ready=true;
            }
            ringProgressDialog.dismiss();
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            if (data_is_ready)
                uploadFileFireBase( file, mStorageRef,biggestSendedID );
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
