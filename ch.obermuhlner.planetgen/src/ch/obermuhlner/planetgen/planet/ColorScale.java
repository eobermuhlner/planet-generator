package ch.obermuhlner.planetgen.planet;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.util.Tuple2;
import ch.obermuhlner.util.Units;

public class ColorScale {

	public static final ColorScale TEMPERATURE_HUMAN_RANGE = new ColorScale()
			.add(Units.celsiusToKelvin(-100), Color.WHITE)
			.add(Units.celsiusToKelvin(-30), Color.AQUA)
			.add(Units.celsiusToKelvin(-10), Color.GREEN)
			.add(Units.celsiusToKelvin(10), Color.YELLOW)
			.add(Units.celsiusToKelvin(30), Color.RED)
			.add(Units.celsiusToKelvin(100), Color.DARKRED);
	
	public static final ColorScale PRECIPITATION_HUMAN_RANGE = new ColorScale()
			.add(0.0, Color.rgb(237, 248, 177))
			.add(0.5, Color.rgb(65, 182, 196))
			.add(1.0, Color.rgb(8, 29, 88));
	
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
