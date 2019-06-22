package com.example.demo.service.repository;


import com.example.demo.model.ChargingPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Hashtable;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ChargingPointsRepository.class})
@TestPropertySource(properties = {"charger.max.count=10"})
public class ChargingPointsRepositoryTest {

    @Autowired
    ChargingPointsRepository chargingPointsRepository;

    @Test
    public void ShouldLoadRepository(){
        assertNotNull(chargingPointsRepository);
    }

    @Test
    public void ShoulReturnListOfChargingPointsFromRepository(){
        assertNotNull(chargingPointsRepository.getCharingPointsList());
    }

    @Test
    public void ShouldReturnListOfChargingPointsWithInitialState(){

        Hashtable<Integer, ChargingPoint> expectedChargingPointsList = new Hashtable<Integer,ChargingPoint>();

        IntStream.range(0,10).forEach(it->{
            ChargingPoint chargingPoint = new ChargingPoint();
            chargingPoint.setStatus("FREE");
            chargingPoint.setPluggedInTime(System.currentTimeMillis());
            expectedChargingPointsList.put(it,chargingPoint);
        });

        assertTrue(chargingPointsRepository.getCharingPointsList().size()==expectedChargingPointsList.size());

        chargingPointsRepository.getCharingPointsList().values().stream().forEach(chargingPoint-> {
            assertTrue(chargingPoint instanceof ChargingPoint);
            assertTrue(chargingPoint.getStatus() == "FREE");
            assertTrue(chargingPoint.getPluggedInTime()<= System.currentTimeMillis());
        });
    }
}
