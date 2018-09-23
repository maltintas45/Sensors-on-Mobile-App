package com.maltintas.zer.sm;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;
import java.util.List;

@Entity
public class Signal {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @TypeConverters( SignalConverter.class )
    @ColumnInfo(name = "x")
    private List<Float> x;

    @TypeConverters( SignalConverter.class )
    @ColumnInfo(name="y")
    private List<Float> y;

    @TypeConverters( SignalConverter.class )
    @ColumnInfo(name="z")
    private List<Float> z;

    @TypeConverters( DateConverter.class )
    @ColumnInfo(name="time")
    private Date time;

    @ColumnInfo(name = "prediction")
    private String prediction;

    public Signal(List<Float> x, List<Float> y, List<Float> z, Date time, String prediction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.time = time;
        this.prediction=prediction;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setX(List<Float> x) {
        this.x = x;
    }

    public void setY(List<Float> y) {
        this.y = y;
    }

    public void setZ(List<Float> z) {
        this.z = z;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public int getId() {
        return id;
    }

    public List<Float> getX() {
        return x;
    }

    public List<Float> getY() {
        return y;
    }

    public List<Float> getZ() {
        return z;
    }

    public Date getTime() {
        return time;
    }

    public String getPrediction() {
        return prediction;
    }
}
