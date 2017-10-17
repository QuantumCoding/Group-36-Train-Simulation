package edu.wit.comp2000.group36.train;

public class Train {
	private static int NEXT_ID = 0;
	private int id;

	private int location;
	private boolean inbound;
	
	private int onboard;
	private final int CAPASITY;
	private PassengerNode passengerRoot;
	
	public Train(boolean isInbound, int startLocation, int capasity) {
		this.id = NEXT_ID ++;
		
		this.inbound = isInbound;
		this.location = startLocation;
		
		this.onboard = 0;
		this.CAPASITY = capasity;
		this.passengerRoot = null;
	}
	
	public void simulate(TrainRoute route) {
//		System.out.println(this + " : " + onboard);
		location = ((inbound ? -- location : ++ location) + route.getLength()) % route.getLength();
		
		Station station = route.getStationAtLocation(location);
		if(station == null) return;
		
		arrivedAt(station);
	}

	public boolean passengerBoard(Passenger passenger) {
		if(onboard >= CAPASITY) return false;
		
		passengerRoot = new PassengerNode(passenger, passengerRoot);
		onboard ++;
		return true;
	}
	
	private void arrivedAt(Station station) {
		PassengerNode node = passengerRoot, prev = null;
		
		while(node != null) {
			if(node.passenger.getDestination().equals(station)) {
				if(prev == null) passengerRoot = node.next;
				else prev.next = node.next;
				
				node.passenger.disembark(this);
			}
			
			prev = node;
			node = node.next;
		}
		
		station.trainArrive(this);
	}
	
	public int getLocation() { return location; }
	public boolean isInbound() { return inbound; }
	public String toString() { return "Train #" + id; }
	
	private static class PassengerNode {
		private Passenger passenger;
		private PassengerNode next;
		
		public PassengerNode(Passenger passenger, PassengerNode next) {
			this.passenger = passenger;
			this.next = next;
		}
	}
}
