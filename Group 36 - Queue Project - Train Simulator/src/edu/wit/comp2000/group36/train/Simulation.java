package edu.wit.comp2000.group36.train;

public class Simulation {
	public static void main(String[] args) {
		Simulation simulation = new Simulation();
		
		for(int i = 0; i < 1_000_000; i ++) {
			simulation.step();
		}
	}
	
	private TrainRoute route;
	private Train[] trains;
	
	public Simulation() {
		int length = 100;
		int[] stationLocations = new int[4];
		
		int maxStep = length / stationLocations.length;
		stationLocations[0] = Logger.RAND.nextInt(maxStep);
		
		for(int i = 1; i < stationLocations.length; i ++) {
			stationLocations[i] = Logger.RAND.nextInt(maxStep) + 1 + stationLocations[i - 1];
		}
		
		route = new TrainRoute(length, stationLocations);
		
		trains = new Train[] {
			new Train(true, 15, 100),
			new Train(true, 30, 100),
			new Train(false, 5, 100),
			new Train(false, 50, 100),
		};
	}
	
	public void step() {
		if(Logger.RAND.nextDouble() > 0.99f) {
			new Passenger(route);
		}
		
		for(Train train : trains) {
			train.simulate(route);
		}
	}
}
