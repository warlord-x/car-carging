package com.example.demo.service.strategy;

import com.example.demo.model.ChargingPoint;

import java.util.Hashtable;

public interface ChargeOptimizer {

    void optimizePlugIn(Hashtable<Integer, ChargingPoint> chargingPointsList, int chargingPointId);
    void optimizeUnplug(Hashtable<Integer, ChargingPoint> chargingPointsList, int chargingPointId);
}
