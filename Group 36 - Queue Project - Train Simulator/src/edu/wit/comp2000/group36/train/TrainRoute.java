package edu.wit.comp2000.group36.train;

public class TrainRoute {
	private int length;
	private Station[] stations;
	
	public TrainRoute(int length, int[] stationLocations) {
		this.length = length;
		this.stations = new Station[stationLocations.length];

		for(int i = 0; i < stationLocations.length; i ++) {
			int location = stationLocations[i];
			if(location > length || location < 0) 
				throw new IllegalArgumentException(location + " Out of Range!");
			
			stations[i] = new Station(location, this);
		}
	}
	
	public Station getStationAtLocation(int location) {
		for(Station station : stations) {
			if(station.getLocation() == location)
				return station;
		}
		
		return null;
	}
	
	public int calcDistance(Station start, Station end, boolean inbound) {
		int startIndex = 0;
		for(; startIndex < stations.length; startIndex ++) {
			if(stations[startIndex] == start) break;
		}
		
		if(startIndex >= stations.length) throw new IllegalArgumentException(start + " is not on this TrainRoute");
		
		int dir = inbound ? -1 : 1;
		int distance = 0, lastLocation = start.getLocation();
		
		for(int i = (startIndex + dir + stations.length) % stations.length; ; i = (i + dir + stations.length) % stations.length) {
			Station check = stations[i];
			
			if(check.getLocation() < lastLocation) { // If the next station wrapped around to the front
				distance += (length - lastLocation) + check.getLocation();
			} else {
				distance += check.getLocation() - lastLocation;
			}
					
			lastLocation = check.getLocation();
		
			if(check == end) {
				return distance;
				
			} else if(check == start) {
				throw new IllegalArgumentException(end + " is not on this TrainRoute");
			}
		}
	}
	
	public int getLength() { return length; }
	public int getStationCount() { return stations.length; }
	public Station getStation(int index) { return stations[index]; }
}
