package edu.wit.comp2000.group36.train;

import com.pearson.carrano.QueueInterface;

public class Station {
	private static int NEXT_ID = 0;
	private int id;
	
	private int location;
	private TrainRoute route;
	
	private QueueInterface<Passenger> inbound;
	private QueueInterface<Passenger> outbound;
	
	private int inboundCount;
	private int outboundCount;
	
	public Station(int location, TrainRoute route) {
		this.id = NEXT_ID ++;
		
		this.location = location;
		this.route = route;
		
		this.inbound = new ArrayQueue<>();
		this.outbound = new ArrayQueue<>();
	}
	
	public void trainArrive(Train train) {
		QueueInterface<Passenger> queue = train.isInbound() ? inbound : outbound;
		
		while(!queue.isEmpty() && train.passengerBoard(queue.getFront())) {
			queue.dequeue().board(train);
			if(train.isInbound()) inboundCount --; else outboundCount --;
		}
	}
	
	public void routPassenger(Passenger passenger) {
		System.out.println(passenger + " ready to Board");
		
		int inboundDist = route.calcDistance(this, passenger.getDestination(), true);
		int outboundDist = route.calcDistance(this, passenger.getDestination(), false);

		if(inboundDist < outboundDist) {
			inbound.enqueue(passenger);
			inboundCount ++;
		} else {
			outbound.enqueue(passenger);
			outboundCount ++;
		}
	}
	
	public int getInboundWaiting() { return inboundCount; }
	public int getOutboundWaiting() { return outboundCount; }
	
	public int getLocation() { return location; }
	public String toString() { return "Station #" + id; }
}
