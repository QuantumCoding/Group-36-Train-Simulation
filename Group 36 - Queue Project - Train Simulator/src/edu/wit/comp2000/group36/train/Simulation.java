package edu.wit.comp2000.group36.train;

import java.util.Arrays;

public class Simulation {
//	public static void main(String[] args) {
//		Simulation simulation = new Simulation();
//		
//		for(int i = 0; i < 1_000_000; i ++) {
//			simulation.step();
//		}
//	}
	
	private TrainRoute route;
	private Train[] trains;
	
	public Simulation() {
		int length = 100;
		int[] stationLocations = new int[4];
		
		int step = length / stationLocations.length;
//		int maxStep = step * 3 / 4, minStep = step / 4;
		stationLocations[0] = Logger.RAND.nextInt(step);
		
		for(int i = 1; i < stationLocations.length; i ++) {
			stationLocations[i] = Logger.RAND.nextInt(step) + step * i;
		}
		
		System.out.println(Arrays.toString(stationLocations));
		
		route = new TrainRoute(length, stationLocations);
		
		trains = new Train[] {
			new Train(true, 15, 100),
//			new Train(true, 75, 100),
//			new Train(false, 5, 100),
//			new Train(false, 50, 100),
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

	public TrainRoute getRoute() { return route; }
	public Train[] getTrains() { return trains; }
}
