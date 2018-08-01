package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class PrevailingWindLayer implements Layer {

	private static final double latitudeCellSize = Planet.RANGE_LATITUDE / 6;

	private static final int[] cellSign = {
		-1,	//  90 ..  60 : west
		1,  //  60 ..  30 : east
		-1, //  30 ..   0 : west
		-1, // -30 ..   0 : west
		1,  // -60 .. -30 : east
		-1  // -90 .. -60 : west
	};

	private static final boolean[] cellDownDirection = {
		true, //  90 ..  60 : down
		false,  //  60 ..  30 : up
		true, //  30 ..   0 : down
		false,  // -30 ..   0 : up
		true, // -60 .. -30 : down
		false   // -90 .. -60 : up
	};

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		int cell = Math.min(cellDownDirection.length - 1, (int) (latitude / latitudeCellSize));
		double cellFraction = latitude % latitudeCellSize / latitudeCellSize;
		
		if (!cellDownDirection[cell]) {
			cellFraction = 1.0 - cellFraction;
		}
		
		planetPoint.prevailingWindStrength = cellFraction;
		planetPoint.prevailingWindAngle = cellSign[cell] * cellFraction * Math.PI + Math.PI * 0.5;
	}
}
