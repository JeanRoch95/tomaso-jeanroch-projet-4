package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        this.calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        long duration = ((outHour - inHour) / 1000 / 60);

        if(duration < 30){
            ticket.setPrice(duration * Fare.VEHICULE_RATE_LESS_THAN_30_MIN);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                    ticket.setPrice(duration * (Fare.CAR_RATE_PER_HOUR) / 60);
                break;
            }
            case BIKE: {
                    ticket.setPrice(duration * (Fare.BIKE_RATE_PER_HOUR) / 60);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        if(discount){
            ticket.setPrice(ticket.getPrice() * Fare.DISCOUNT_RATE);
        }
    }
}