package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;

public class CraterLayer implements Layer {

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {

		double craterHeight = crater(latitude, longitude, 4);
		layerState.height += craterHeight;
	}

	private double crater(double latitude, double longitude, double grid) {
//		double latitudeSector = Planet.RANGE_LATITUDE * grid;
//		double longitudeSector = Planet.RANGE_LONGITUDE * grid;
//		
//		double latitudeSectorFloor = Math.floor(latitudeSector);
//		double longitudeSectorFloor = Math.floor(longitudeSector);
//
//		double latitudeSectorFract = latitudeSector - latitudeSectorFloor;
//		double longitudeSectorFract = longitudeSector - longitudeSectorFloor;
//		
//		double radius = 0.3;
//		double distance = length(latitudeSectorFract - 0.5, longitudeSectorFract - 0.5);
//
//		return craterFunction(radius, distance) * 1000;
		
		double latitudeCrater = 0.5;
		double longitudeCrater = 0.5;
		
		double latitudeDistance = (latitude-latitudeCrater) / Planet.RANGE_LATITUDE;
		double longitudeDistance = (longitude-longitudeCrater) / Planet.RANGE_LONGITUDE;
		double distance = length(latitudeDistance, longitudeDistance);
		if (distance < 0.25) {
			return distance * 10000;
		} else {
			return 0;
		}
	}
	
	private double craterFunction(double radius, double distance) {
		return distance;
	}

	private static double length(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}

}
