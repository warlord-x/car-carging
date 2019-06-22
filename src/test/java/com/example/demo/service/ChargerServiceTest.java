package com.example.demo.service;

import com.example.demo.exceptions.InvalidCharingPointException;
import com.example.demo.model.ChargingPoint;
import com.example.demo.service.repository.ChargingPointsRepository;
import com.example.demo.service.strategy.LastPluginTimeOptimizer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ChargerService.class, LastPluginTimeOptimizer.class, ChargingPointsRepository.class})
@TestPropertySource(properties = {"charger.max.count=10"})
public class ChargerServiceTest {

    @Autowired
    ChargerService chargerService;

    @Before
    public void reset(){
        chargerService = new ChargerService(new ChargingPointsRepository(10), new LastPluginTimeOptimizer(10));
    }

    @Test
    public void ShouldReturnListOfChargingPointsWithInitialState(){

        Hashtable<Integer,ChargingPoint> expectedChargingPointsList = new Hashtable<Integer,ChargingPoint>();

        IntStream.range(0,10).forEach(it->{
            ChargingPoint chargingPoint = new ChargingPoint();
            chargingPoint.setStatus("FREE");
            chargingPoint.setPluggedInTime(System.currentTimeMillis());
            expectedChargingPointsList.put(it,chargingPoint);
        });

        assertTrue(chargerService.getChargingPoints().size()==expectedChargingPointsList.size());

        chargerService.getChargingPoints().values().stream().forEach(chargingPoint-> {
            assertTrue(chargingPoint instanceof ChargingPoint);
            assertTrue(chargingPoint.getStatus() == "FREE");
            assertTrue(chargingPoint.getPluggedInTime()<= System.currentTimeMillis());
        });
    }

    @Test
    public void ShouldMaintainChargerListWithDynamicCount(){
        assertTrue(chargerService.getChargingPoints().values().size() == 10);
    }

    @Test
    public void ShouldUpdateTheChargingStatusWhenPluggedForSpecificId() throws InvalidCharingPointException {
        chargerService.plug(5);
        assertTrue(chargerService.getChargingPoints().get(4).getStatus() == "FAST_CHARGING");
    }

    @Test
    public void ShouldReturnErrorWhenUpdateStatusIsCalledWithIncorrectId()  {

        int[] pointIdInput = new int[] {-1,22,33};
        Arrays.stream(pointIdInput).forEach(
                id -> {

                    try {
                        chargerService.plug(id);
                    } catch (InvalidCharingPointException e) {
                        assertTrue(e.getLocalizedMessage().matches("Invalid charging point:"+id));
                    }
                }
        );

    }
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Test
    public void ShouldReturnErrorWhenPlugIsCalledForAlreadyPluggedInCharger() throws Exception  {
        exceptionRule.expect(InvalidCharingPointException.class);
        //exceptionRule.expectMessage("Invalid charging point:7");
        exceptionRule.expectMessage("Charging point already plugged");
        chargerService.plug(7);
        chargerService.plug(7);
    }

    @Test
    public void ShouldChargeWithFastChargingWhenCapacityIsAvailable()  {
        plugInRange(6,true);
        IntStream.range(1, 5).forEach(id->
                assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING")
        );
    }


    @Test
    public void ShouldMaintainPluggedInTimeWithStatusOfEachChargingPoint(){
        try {
            chargerService.plug(1);
        }
        catch (Exception ex){
            fail();
        }
        assertNotNull(chargerService.getChargingPoints().get(0).getPluggedInTime());
    }

    @Test
    public void ShouldChargeWithFastChargingAndReduceTwoOldestHighChargingToLowWhenAllWereChargingWithFastCharge(){
        plugInRange(7,true);
        assertTrue(chargerService.getChargingPoints().get(5).getStatus()=="FAST_CHARGING");
        assertTrue(chargerService.getChargingPoints().get(0).getStatus()=="SLOW_CHARGING");
        assertTrue(chargerService.getChargingPoints().get(1).getStatus()=="SLOW_CHARGING");
    }


    @Test
    public void ShouldChargeWithFastChargingAndReduceOldestHighChargingToLowWhenCapacityWasFullyUsed(){
       // LLHHHH -> LLLLHHH
        plugInRange(8,true);
        assertTrue(chargerService.getChargingPoints().get(6).getStatus()=="FAST_CHARGING");
        IntStream.range(0,4).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="SLOW_CHARGING"));

    }

    @Test
    public void ShouldChargeAllWithLowWhenMaximumArePlugged(){
        // EEEEEEEEEE -> LLLLLLLLLL
        plugInRange(11,true);

        IntStream.range(0,10).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="SLOW_CHARGING"));
    }

    @Test
    public void ShouldThrowExceptionWhenAndEmptyChargingPointIsCalledForUnplug() throws InvalidCharingPointException{
        exceptionRule.expect(InvalidCharingPointException.class);
        exceptionRule.expectMessage("Charging Point already unplugged");
        plugInRange(6,true);
        IntStream.range(0,5).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        chargerService.unPlug(9);

    }

    @Test
    public void ShouldNotChangeStatusOfRestWhenAllWereHighChargingAndOneGetsUnplugged() throws InvalidCharingPointException{
        //HHHHH -> HHHH
        plugInRange(6,true);
        IntStream.range(0,5).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        chargerService.unPlug(1);
        IntStream.range(1,4).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        assertTrue(chargerService.getChargingPoints().get(0).getStatus()=="FREE");
        IntStream.range(5,10).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FREE"));

    }

    @Test
    public void ShouldUpgradeLastPluggedLowChargingOneToHighWhenOneLowChargingOneGetsUnplugged() throws InvalidCharingPointException{
        //LLHHHH -> EHHHHH
        plugInRange(7,true);
        IntStream.range(2,5).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));

        chargerService.unPlug(1); //unplug one
        IntStream.range(1,6).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        IntStream.range(6,10).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FREE"));

    }

    @Test
    public void ShouldUpgradeAllPossibleLastPluggedLowChargingOneToHighWhenManyLowChargingOneGetsUnplugged(){
        //LLLLHHH -> EEHHHHH
        plugInRange(7,true);
        IntStream.range(2,5).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        try {
            chargerService.unPlug(1); //unplug one
        }
        catch (InvalidCharingPointException ex){
            fail();
        }
        IntStream.range(1,6).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FAST_CHARGING"));
        IntStream.range(6,10).forEach(id->assertTrue(chargerService.getChargingPoints().get(id).getStatus()=="FREE"));

    }

    //TODO : Check for only the last plugged gets upgarded


    @Test
    public void ShouldUpdateTheChargingPointListSequentially() throws BrokenBarrierException, InterruptedException {

        plugInRange(10,true);
       // final CyclicBarrier gate = new CyclicBarrier(11);
        final CyclicBarrier gate = new CyclicBarrier(3);

        /*for(int i=0;i<10;i++){
            new Thread(()->{
                try {
                    gate.await();

                    chargerService.unPlug(1);
                    chargerService.plug(1);
                }
                catch (InvalidCharingPointException | InterruptedException | BrokenBarrierException ex){
                    System.out.println("error"+ex.getMessage());
                }
            }).start();
        }*/

        Thread t1 = new Thread(){
            public void run(){
                try {
                    gate.await();
                    chargerService.unPlug(1);
                } catch (InterruptedException e) {
                    System.out.println("aaaaa");
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.out.println("aaaaa");
                } catch (InvalidCharingPointException e) {
                    e.printStackTrace();
                    System.out.println("aaaaa");
                }
                //do stuff
            }};
        Thread t2 = new Thread(){
            public void run(){
                try {
                    gate.await();
                    chargerService.plug(1);
                } catch (InterruptedException | InvalidCharingPointException e) {
                    e.printStackTrace();
                    System.out.println("bbbb");
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                    System.out.println("bbbb");
                }
                //do stuff
            }};

        t1.start();
        t2.start();



        gate.await();

    }

    private void plugInRange(int i,boolean plug) {
        IntStream.range(1, i).forEach(id -> {
            try {
                if(plug) {
                    chargerService.plug(id);
                    Thread.sleep(2);
                }
                else
                    chargerService.unPlug(id);
            } catch (Exception ex) {
                fail();
            }
        });
    }
}




/*

// Improvements... Refactor optimize logic to a pluggable algorithm showing strategy pattern

 */