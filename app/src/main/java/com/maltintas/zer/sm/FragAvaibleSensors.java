package com.maltintas.zer.sm;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.maltintas.zer.sm.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragAvaibleSensors extends Fragment {




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_avaiblesensors,container,false);
        //TextView textView=view.findViewById(R.id.explanation);

        SensorManager smm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        final List<Sensor> sensors = smm.getSensorList( Sensor.TYPE_ALL);


        // Array of strings for ListView Title
        final String[] listviewTitle = new String[sensors.size()];
        int[] listviewImage = new int[sensors.size()];
        String[] listviewShortDescription = new String[sensors.size()];
        int j=0;
        for (Sensor sensor : sensors)
        {
            listviewTitle[j]=sensor.getName();
            listviewImage[j]=R.drawable.ic_launcher_background;
            listviewShortDescription[j]=sensor.toString();
            j++;
        }

        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < sensors.size(); i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("listview_title", listviewTitle[i]);
            hm.put("listview_discription", listviewShortDescription[i]);
            hm.put("listview_image", Integer.toString(listviewImage[i]));
            aList.add(hm);
        }

        String[] from = {"listview_image", "listview_title", "listview_discription"};
        int[] to = {R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description};

        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), aList, R.layout.listview_activity, from, to);
        ListView androidListView = (ListView) view.findViewById(R.id.list_view);
        androidListView.setAdapter(simpleAdapter);
        androidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String links = "http://www.google.com/search?"+"q="+listviewTitle[position].toLowerCase()+"+sensors";
                Uri uri = Uri.parse(links);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //textView.setText( getString(R.string.sensor_expalanation, sensors.size()) );
        return view;
    }
}
