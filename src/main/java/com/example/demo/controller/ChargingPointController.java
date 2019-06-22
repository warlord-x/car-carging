package com.example.demo.controller;

import com.example.demo.exceptions.InvalidCharingPointException;
import com.example.demo.model.ChargingPoint;
import com.example.demo.service.ChargerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Hashtable;

@RestController
public class ChargingPointController {

    @Autowired
    ChargerService chargerService;


    @RequestMapping("/getstatus")
    public Hashtable<Integer,ChargingPoint> getChargerList() {
            return chargerService.getChargingPoints();
    }

    @GetMapping("/plug/{chargingId}")
    public Hashtable<Integer,ChargingPoint> plug(@PathVariable Integer chargingId) throws InvalidCharingPointException {
            return chargerService.plug(chargingId);
    }
    @GetMapping("/unplug/{chargingId}")
    public Hashtable<Integer,ChargingPoint> unplug(@PathVariable Integer chargingId) throws InvalidCharingPointException {
            return chargerService.unPlug(chargingId);
    }
}
