package com.maltintas.zer.sm;

import android.annotation.TargetApi;
import android.arch.persistence.room.TypeConverter;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class SignalConverter {
    Gson gson = new Gson();

    @TypeConverter
    public List<Float> stringToSomeObjectList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Float>>() {}.getType();
        try {
            return gson.fromJson( data, listType );
        }
        catch(Exception e){
            return null;
        }
    }

    @TypeConverter
    public String someObjectListToString(List<Float> someObjects) {
        try{
            return gson.toJson(someObjects);}
        catch(Exception e){
            return e.toString();
        }
    }
}
