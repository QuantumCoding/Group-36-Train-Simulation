package edu.wit.comp2000.group36.train;

import java.util.Iterator;
import java.util.LinkedList;

public class Train {
	private static boolean atStation;
	
	public static boolean atStation() { return atStation; }
	public static void atStationReset() { atStation = false; }
	
	private static int NEXT_ID = 0;
	private int id;

	private int location;
	private boolean inbound;
	
	private int justBoarded;
	private final int CAPASITY;
	private LinkedList<Passenger> passenegers;
	
	public Train(boolean isInbound, int startLocation, int capasity) {
		this.id = NEXT_ID ++;
		
		this.inbound = isInbound;
		this.location = startLocation;
		
		this.CAPASITY = capasity;
		this.passenegers = new LinkedList<>();
	}
	
	public void simulate(TrainRoute route) {
		justBoarded = 0;
		location = ((inbound ? -- location : ++ location) + route.getLength()) % route.getLength();
		
		Station station = route.getStationAtLocation(location);
		if(station == null) return;
		atStation = true;
		
		arrivedAt(station);
	}

	public boolean passengerBoard(Passenger passenger) {
		if(passenegers.size() >= CAPASITY) return false;
		
		passenegers.add(passenger);
		justBoarded ++;
		return true;
	}
	
	private void arrivedAt(Station station) {
		for(Iterator<Passenger> iter = passenegers.iterator(); iter.hasNext();) {
			Passenger passenger = iter.next();
			if(passenger.getDestination().equals(station)) {
				iter.remove();
				passenger.disembark(this);
			}
		}
		
		station.trainArrive(this);
	}
	
	public int getPassengerCount() { return passenegers.size(); }
	public int getJustBoarded() { return justBoarded; }
	
	public int getLocation() { return location; }
	public boolean isInbound() { return inbound; }
	public String toString() { return "Train #" + id; }
}
