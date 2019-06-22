package com.example.demo.service.strategy;

import com.example.demo.model.ChargingPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.util.ChargingConstants.*;

@Component
public class LastPluginTimeOptimizer implements ChargeOptimizer{

    int chargerCount = 0;
    public LastPluginTimeOptimizer(@Value("${charger.max.count}") int chargersCount){
        this.chargerCount = chargersCount;
    }

    @Override
    public void optimizePlugIn(Hashtable<Integer, ChargingPoint> chargingPointsList, int chargingPointId) {
        ChargingPoint chargingPoint = new ChargingPoint();
        chargingPoint.setStatus(FAST_CHARGING);
        chargingPoint.setPluggedInTime(System.currentTimeMillis());



        if(getAvailableCapacity(chargingPointsList) > chargerCount){
            chargingPoint.setStatus(FAST_CHARGING);
            chargingPointsList.put(chargingPointId-1,chargingPoint);
        }
        else if(getAvailableCapacity(chargingPointsList)<chargerCount) {
            List<ChargingPoint> sortedList = getSortedListByOldestChargingPointWithHighCharging(chargingPointsList);
            if(sortedList.size()>1){
                sortedList.get(0).setStatus(SLOW_CHARGING);
                sortedList.get(1).setStatus(SLOW_CHARGING);
                chargingPoint.setStatus(FAST_CHARGING);
                chargingPointsList.put(chargingPointId-1,chargingPoint);
            }
            if(sortedList.size()==1){
                sortedList.get(0).setStatus(SLOW_CHARGING);
                chargingPoint.setStatus(SLOW_CHARGING);
                chargingPointsList.put(chargingPointId-1,chargingPoint);

            }
        }
    }

    @Override
    public void optimizeUnplug(Hashtable<Integer, ChargingPoint> chargingPointsList, int chargingPointId) {
        chargingPointsList.get(chargingPointId-1).setStatus(FREE);
        chargingPointsList.get(chargingPointId-1).setPluggedInTime(0);
        while(getAvailableCapacity(chargingPointsList)>=chargerCount) { // considering all capacity used up
            Optional<ChargingPoint> firstLowest = chargingPointsList.values().stream()
                    .filter(chargingPoint -> chargingPoint.getStatus() == SLOW_CHARGING)
                    .sorted(Comparator.comparingLong(ChargingPoint::getPluggedInTime).reversed())
                    .findFirst();
            if(firstLowest.isPresent())
                firstLowest.get().setStatus(FAST_CHARGING);
            else break;
        }
    }

    private List<ChargingPoint> getSortedListByOldestChargingPointWithHighCharging(Hashtable<Integer, ChargingPoint> chargingPointsList){
        return chargingPointsList.values().stream()
                .filter(chargingPoint -> chargingPoint.getStatus()== FAST_CHARGING)
                .sorted(Comparator.comparingLong(ChargingPoint::getPluggedInTime)) //TODO : reverse order ???
                //.sorted(Comparator.comparing(ChargingPoint::getStatus)) //TODO : NOT NEEDED SINCE FILTERED
                .collect(Collectors.toList());
    }

    private int getAvailableCapacity (Hashtable<Integer, ChargingPoint> chargingPointsList){
        int usedCapacity = 0;


        //TODO : Change to stream and lambda
        for (ChargingPoint chargingPoint:chargingPointsList.values()) {
            if( chargingPoint.getStatus() == FAST_CHARGING)
                usedCapacity = usedCapacity+20;
            if( chargingPoint.getStatus() == SLOW_CHARGING)
                usedCapacity = usedCapacity+10;
        }

        return  TOTAL_CAPACITY-usedCapacity;

    }


}
