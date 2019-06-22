package com.example.demo.service.repository;

import com.example.demo.model.ChargingPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Hashtable;
import java.util.stream.IntStream;

import static com.example.demo.util.ChargingConstants.FREE;

@Repository
public class ChargingPointsRepository {

    private Hashtable<Integer, ChargingPoint> chargingPointsList;

    public ChargingPointsRepository(@Value("${charger.max.count}") int chargersCount){
        chargingPointsList = new Hashtable<>();
        IntStream.range(0, chargersCount).forEach(it -> chargingPointsList.put(it, createFreeChargingPoint()));
    }

    public Hashtable<Integer, ChargingPoint> getCharingPointsList() {
        return chargingPointsList;
    }

    private ChargingPoint createFreeChargingPoint() {
        ChargingPoint chargingPoint = new ChargingPoint();
        chargingPoint.setStatus(FREE);
        chargingPoint.setPluggedInTime(0);
        return chargingPoint;
    }
}
