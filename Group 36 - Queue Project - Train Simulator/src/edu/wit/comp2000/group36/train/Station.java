package edu.wit.comp2000.group36.train;

import com.pearson.carrano.QueueInterface;

public class Station {
	private int location;
	private TrainRoute route;
	
	private QueueInterface<Passenger> inbound;
	private QueueInterface<Passenger> outbound;
	
	public Station(int location, TrainRoute route) {
		this.location = location;
		this.route = route;
		
		this.inbound = new ArrayQueue<>();
		this.outbound = new ArrayQueue<>();
	}
	
	public void trainArrive(Train train) {
		QueueInterface<Passenger> queue = train.isInbound() ? inbound : outbound;
		
		while(!queue.isEmpty() && train.passengerBoard(queue.getFront())) {
			queue.dequeue().board(train);
		}
	}
	
	public void routPassenger(Passenger passenger) {
		System.out.println(passenger + " ready to Board");
		
		int inboundDist = route.calcDistance(this, passenger.getDestination(), true);
		int outboundDist = route.calcDistance(this, passenger.getDestination(), false);
		
		if(inboundDist < outboundDist) {
			inbound.enqueue(passenger);
		} else {
			outbound.enqueue(passenger);
		}
	}
	
	public int getLocation() { return location; }
}
