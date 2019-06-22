package com.example.demo.service;

import com.example.demo.exceptions.InvalidCharingPointException;
import com.example.demo.model.ChargingPoint;
import com.example.demo.service.repository.ChargingPointsRepository;
import com.example.demo.service.strategy.ChargeOptimizer;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

import static com.example.demo.util.ChargingConstants.*;

@Service
public class ChargerService {

    ChargeOptimizer chargeOptimizer;
    ChargingPointsRepository chargingPointsRepository ;


    public ChargerService( ChargingPointsRepository chargingPointsRepository,ChargeOptimizer lastPluginTimeOptimizer) {
        this.chargingPointsRepository = chargingPointsRepository;
        this.chargeOptimizer = lastPluginTimeOptimizer;
    }

    public Hashtable<Integer, ChargingPoint> getChargingPoints() {
        return chargingPointsRepository.getCharingPointsList();
    }

    public Hashtable<Integer, ChargingPoint> plug(int chargingPointId) throws InvalidCharingPointException {
        validatePlugInPoint(chargingPointId,true);
        chargeOptimizer.optimizePlugIn(chargingPointsRepository.getCharingPointsList(), chargingPointId);
        return chargingPointsRepository.getCharingPointsList();
    }


    public Hashtable<Integer, ChargingPoint> unPlug(int chargingPointId) throws InvalidCharingPointException {
        validatePlugInPoint(chargingPointId,false);
        chargeOptimizer.optimizeUnplug(chargingPointsRepository.getCharingPointsList(), chargingPointId);
        return chargingPointsRepository.getCharingPointsList();
    }

    //TODO : annotation for validator ?
    private void validatePlugInPoint(int chargingPointId, boolean isPlug) throws InvalidCharingPointException {
        if (chargingPointId < 0 || chargingPointId > chargingPointsRepository.getCharingPointsList().size())
            throw new InvalidCharingPointException(INVALID_CHARGING_POINT + chargingPointId);
        if (isPlug) {
            if (!isChargingPointEmpty(chargingPointId))
                throw new InvalidCharingPointException(CHARGING_POINT_ALREADY_PLUGGED_MESSAGE);
        } else {
            if (isChargingPointEmpty(chargingPointId))
                throw new InvalidCharingPointException(CHARGING_POINT_ALREADY_UNPLUGGED_MESSAGE);
        }
    }

    private boolean isChargingPointEmpty(int chargingPointId) {
        if (chargingPointsRepository.getCharingPointsList().get(chargingPointId - 1).getStatus() != FREE) return false;
        return true;
    }

}
