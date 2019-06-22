package com.example.demo.controller;

import com.example.demo.exceptions.ChargerPointExceptionHandler;
import com.example.demo.exceptions.InvalidCharingPointException;
import com.example.demo.model.ChargingPoint;
import com.example.demo.service.ChargerService;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*@RunWith(SpringRunner.class)
@SpringBootTest*/
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ChargingPointController.class)

public class ChargingPointControllerTest {

    @Autowired
    private ChargingPointController chargingPointController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChargerService chargerService;

    @Test
    public void contexLoads() throws Exception {
        assertThat(chargingPointController).isNotNull();
    }

    @Test
    public void shouldReturnChargingPointsList() throws Exception {

        when(chargerService.getChargingPoints())
                .thenReturn(getExpectedChargingPointList());

        this.mockMvc.perform(get("/getstatus")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.0.status",containsString("FAST_CHARGING")));

    }
    @Test
    public void shouldReturnChargingPointsListWithDateInReadableFormat() throws Exception {

        when(chargerService.getChargingPoints())
                .thenReturn(getExpectedChargingPointList());

        this.mockMvc.perform(get("/getstatus")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.0.pluggedInTime", new IsDate()));

    }

    @Test
    public void shouldReturnChargingPointsListWhenChargingPluggedIn() throws Exception {

        int validChargingPoint = 1;
        when(chargerService.plug(validChargingPoint))
                .thenReturn(getExpectedChargingPointList());

        mockMvc.perform(get("/plug/"+validChargingPoint)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.0.status",containsString("FAST_CHARGING")));

    }
    @Test
    public void shouldReturnExpectedChargingPointsListWhenChargingIsUnplugged() throws Exception {

        int validChargingPoint = 1;
        when(chargerService.unPlug(validChargingPoint))
                .thenReturn(getExpectedChargingPointList());

        mockMvc.perform(get("/unplug/"+validChargingPoint)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.0.status",containsString("FAST_CHARGING")));

    }

    @Test
    public void shouldReturnInternalServerErrorWhenExceptionIsThrownByService() throws Exception {


        when(chargerService.getChargingPoints())
                .thenThrow(new NullPointerException("some-error-message"));

        MockMvc mockMvcWithExceptionHandler = MockMvcBuilders.standaloneSetup(chargingPointController)
                .setControllerAdvice(new ChargerPointExceptionHandler()).build();
        mockMvcWithExceptionHandler.perform(get("/getstatus")).andDo(print()).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage",containsString("some-error-message")));

    }

    @Test
    public void shouldReturnBadRequestWhenInvalidChargingPointExceptionIsThrownByService() throws Exception {

        int invalidChargingPoint = 77;
        when(chargerService.plug(invalidChargingPoint))
                .thenThrow(new InvalidCharingPointException("Invalid charging point:"+invalidChargingPoint));

        MockMvc mockMvcWithExceptionHandler = MockMvcBuilders.standaloneSetup(chargingPointController)
                .setControllerAdvice(new ChargerPointExceptionHandler()).build();
        mockMvcWithExceptionHandler.perform(get("/plug/"+invalidChargingPoint)).andDo(print()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage",containsString("Invalid charging point:"+invalidChargingPoint)));

    }

    private Hashtable<Integer, ChargingPoint> getExpectedChargingPointList() {
        Hashtable<Integer,ChargingPoint> expectedChargingPoints = new Hashtable<>();
        ChargingPoint chargingPoint = new ChargingPoint();
        chargingPoint.setStatus("FAST_CHARGING");
        chargingPoint.setPluggedInTime(System.currentTimeMillis());
        expectedChargingPoints.put(0,chargingPoint);
        return expectedChargingPoints;
    }

}

class IsDate extends TypeSafeMatcher<String> {

    @Override
    protected boolean matchesSafely(String s) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setLenient(true);
            df.parse(s);
            return true;
        } catch (ParseException nfe){
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("only date");
    }
}
