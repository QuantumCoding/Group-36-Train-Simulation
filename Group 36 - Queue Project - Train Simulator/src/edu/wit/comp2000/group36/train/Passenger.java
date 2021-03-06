package edu.wit.comp2000.group36.train;

public class Passenger {
	private static int NEXT_ID = 0;
	
	private int id;
	private Station dest;
	
	public Passenger(TrainRoute route) {
		id = NEXT_ID ++;
		
		int count = route.getStationCount();
		int startIndex = Logger.RAND.nextInt(count);
		int destIndex = (Logger.RAND.nextInt(count - 1) + 1 + startIndex) % count;
	
		this.dest = route.getStation(destIndex);
		route.getStation(startIndex).routPassenger(this);
	}
	
	public void disembark(Train train) { }
	public void board(Train train) { }
	
	public Station getDestination() { return dest; }
	public String toString() { return "Passenger #" + id; }
}
