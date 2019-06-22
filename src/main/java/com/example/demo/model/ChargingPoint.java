package com.example.demo.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class ChargingPoint {

    @JsonSerialize(using = CustomDateSerializer.class)
    private long pluggedInTime;
    private String status;
}
class CustomDateSerializer extends JsonSerializer {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        long seconds = (long)value;
        if(seconds==0)
        gen.writeString("-");
       else{
            Date date = new Date((long) value);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            gen.writeString(df.format(date));
        }
    }
}
