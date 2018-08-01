package ch.obermuhlner.planetgen.planet;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.util.Tuple2;
import ch.obermuhlner.util.Units;

public class ColorScale {

	
	/**
	 * Temperature in Kelvin.
	 */
	public static final ColorScale TEMPERATURE_HUMAN_RANGE = new ColorScale()
			.add(Units.celsiusToKelvin(-50), Color.DARKBLUE)
			.add(Units.celsiusToKelvin(0), Color.WHITE)
			.add(Units.celsiusToKelvin(50), Color.DARKRED);

	/**
	 * 1.0 is normal atmospheric pressure at ocean level.
	 * Typical variation is +/- 5%.
	 */
	public static final ColorScale ATMOSPHERIC_PRESSURE_HUMAN_RANGE = new ColorScale()
			.add(0.95, Color.DARKBLUE)
			.add(1.00, Color.WHITE)
			.add(1.05, Color.DARKRED);

	public static final ColorScale PRECIPITATION_HUMAN_RANGE = new ColorScale()
			.add(0.0, Color.WHITE)
			.add(1.0, Color.DARKRED)
			.add(2.0, Color.YELLOW);

	public static ColorScale divergingScale(double min, double center, double max) {
		return new ColorScale()
				.add(min, Color.DARKBLUE)
				.add(center, Color.WHITE)
				.add(max, Color.DARKRED);
	}
	
	private final List<Tuple2<Double, Color>> valuesToColors = new ArrayList<>();

	public ColorScale add(double value, Color color) {
		valuesToColors.add(Tuple2.of(value, color));
		return this;
	}
	
	public Color toColor(double value) {
		double beginValue = valuesToColors.get(0).getValue1();
		Color beginColor = valuesToColors.get(0).getValue2();
		
		for (Tuple2<Double, Color> valueToColor : valuesToColors) {
			double endValue = valueToColor.getValue1();
			Color endColor = valueToColor.getValue2();
			
			if (value < endValue) {
				double range = endValue - beginValue;
				if (range != 0) {
					double weight = (value - beginValue) / range;
					return beginColor.interpolate(endColor, weight);
				} else {
					return beginColor;
				}
			}
			
			beginValue = endValue;
			beginColor = endColor;
		}
		
		return beginColor;
	}
}
