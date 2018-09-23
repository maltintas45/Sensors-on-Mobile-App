package com.maltintas.zer.sm;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SignalDao {
    @Query( "SELECT * from signal" )
    List<Signal> getAllSignals();
    @Insert
    void insertSignal(Signal... signal);
}
