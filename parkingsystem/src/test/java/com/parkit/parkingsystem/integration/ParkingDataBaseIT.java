package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;

import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


import java.util.Date;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar()throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());

        assertEquals(ticket.getId(), 1);
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), 2);

        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit()throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1002));
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(inputReaderUtil.readVehicleRegistrationNumber());
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF");

        assertEquals(1, ticketDAO.getNbTicket(inputReaderUtil.readVehicleRegistrationNumber()));
        assertNotNull(ticket.getOutTime());
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingLotExitRecurringUser()throws Exception{
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(inputReaderUtil.readVehicleRegistrationNumber());
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF");

        assertEquals(2, ticketDAO.getNbTicket(inputReaderUtil.readVehicleRegistrationNumber()));
        assertEquals(Fare.CAR_RATE_PER_HOUR * Fare.DISCOUNT_RATE, ticket.getPrice());

    }

}
