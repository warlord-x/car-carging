package com.example.demo.exceptions;




    public class InvalidCharingPointException extends Exception{

        public InvalidCharingPointException(String cause){
            //super("Invalid charging point:"+chargingPointId);
            super(cause);
           //this.chargingPointId = chargingPointId;
        }
        //private int chargingPointId;

    }

