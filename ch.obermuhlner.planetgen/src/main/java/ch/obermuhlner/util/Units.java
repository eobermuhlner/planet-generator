package ch.obermuhlner.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Units {
	
	public static final double SECONDS_PER_HOUR = 60*60;
	public static final double SECONDS_PER_DAY = 24*60*60;
	public static final double SECONDS_PER_YEAR = SECONDS_PER_DAY * 365.25;
	public static final double LIGHT_SECOND = 299792458;
	public static final double LIGHT_YEAR = LIGHT_SECOND * SECONDS_PER_YEAR;
	public static final double ASTRONOMICAL_UNIT = 149597871E3;

	private static final double CELSIUS_BASE = 273.16;
	private static final MathContext[] MC = { 
			new MathContext(0, RoundingMode.HALF_UP),
			new MathContext(1, RoundingMode.HALF_UP),
			new MathContext(2, RoundingMode.HALF_UP),
			new MathContext(3, RoundingMode.HALF_UP),
			new MathContext(4, RoundingMode.HALF_UP),
			new MathContext(5, RoundingMode.HALF_UP),
			new MathContext(6, RoundingMode.HALF_UP),
			new MathContext(7, RoundingMode.HALF_UP),
			new MathContext(8, RoundingMode.HALF_UP),
			new MathContext(9, RoundingMode.HALF_UP),
	};

	/**
	 * See: http://en.wikipedia.org/wiki/Stefan%E2%80%93Boltzmann_constant
	 * W*m^-2*K^-4
	 */
	public static final double STEFAN_BOLTZMAN_CONSTANT = 5.670373E-8;
	
	public static final double EARTH_ORBIT_RADIUS = 149597890E3;
	public static final double EARTH_ORBIT_PERIOD = 1 * SECONDS_PER_YEAR;
	public static final double EARTH_MASS = 5.9742E24;
	public static final double EARTH_RADIUS = 6378.1E3;
	public static final double EARTH_DENSITY = EARTH_MASS / volumeSphere(EARTH_RADIUS);
	public static final double EARTH_PERIOD = 1 * SECONDS_PER_DAY;
	public static final double EARTH_ATMOSPHERE_PRESSURE = 101.325E3; //Pa
	
	public static final double JUPITER_MASS = 1.8987E27;
	public static final double JUPITER_RADIUS = 71492.68E3;
	public static final double JUPITER_DENSITY = JUPITER_MASS / volumeSphere(JUPITER_RADIUS);

	public static final double NEPTUNE_MASS = 1.0244E26;
	public static final double NEPTUNE_RADIUS = 24766.36E3;
	public static final double NEPTUNE_DENSITY = NEPTUNE_MASS / volumeSphere(NEPTUNE_RADIUS);

	public static final double SUN_MASS = 2E30;
	public static final double SUN_RADIUS = 700000E3;
	public static final double SUN_DENSITY = SUN_MASS / volumeSphere(SUN_RADIUS);
	public static final double SUN_LUMINOSITY = 3.827E26; // W

	public static final double MOON_MASS = 7.3477E22;
	public static final double GRAVITATIONAL_CONSTANT = 6.67408E-11; // m^3 * kg^-1 * s^-2

	private static Unit meterUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(1000, "km"),
		new Unit(1, "m"),
	};

	private static Unit meterDistanceUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(LIGHT_SECOND, "lightseconds"),
		new Unit(1000, "km"),
		new Unit(1, "m"),
	};

	private static Unit alternateSizeUnits[] = {
		new Unit(SUN_RADIUS, "sun radius"),
		new Unit(JUPITER_RADIUS, "jupiter radius"),
		new Unit(EARTH_RADIUS, "earth radius"),
	};

	private static Unit alternate1OrbitUnits[] = {
		new Unit(LIGHT_YEAR, "lightyears"),
		new Unit(LIGHT_SECOND, "lightseconds"),
	};

	private static Unit alternate2OrbitUnits[] = {
		new Unit(ASTRONOMICAL_UNIT, "earth orbits"),
	};

	private static Unit secondUnits[] = {
		new Unit(365.25*24*60*60, "years"),
		new Unit(24*60*60, "days"),
		new Unit(60*60, "hours"),
		new Unit(60, "minutes"),
		new Unit(1, "seconds"),
	};

	private static Unit kilogramUnits[] = {
		new Unit(1E12, "Gt"),
		new Unit(1E9, "Mt"),
		new Unit(1E6, "kt"),
		new Unit(1E3, "t"),
		new Unit(1, "kg"),
		new Unit(0.001, "g"),
	};
	
	private static Unit alternateKilogramUnits[] = {
		new Unit(SUN_MASS, "sun mass"),
		new Unit(JUPITER_MASS, "jupiter mass"),
		new Unit(EARTH_MASS, "earth mass"),
		new Unit(MOON_MASS, "moon mass"),
	};

	private static Unit newtonUnits[] = {
			new Unit(1, "N"),
		};

	private static Unit speedUnits[] = {
		new Unit(1000, "km/s"),
		new Unit(1, "m/s"),
	};

	private static Unit alternateNewtonGravityUnits[] = {
		new Unit(gravity(SUN_MASS, SUN_RADIUS), "sun gravities"),
		new Unit(gravity(EARTH_MASS, EARTH_RADIUS), "earth gravities"),
	};

	public static String toString(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return "---";
		}
		
		return toString(new BigDecimal(value).round(MC[3]));
	}
	
	public static String toString(double value, int significantDigits) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return "---";
		}

		MathContext mathContext = significantDigits<MC.length ? MC[significantDigits] : new MathContext(significantDigits, RoundingMode.HALF_UP);
		return toString(new BigDecimal(value).round(mathContext));
	}

	public static String toString(BigDecimal value) {
		String plain = value.toPlainString();
		if (plain.length() < 8) {
			return plain;
		}
		return value.toString();
	}
	
	public static String toString(double value, int leftDigits, int rightDigits) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		writer.format("%" + leftDigits + "." + rightDigits + "f", value);
		return stringWriter.toString();
	}
	
	public static String meterSizeToString(double value) {
		return unitToString(value, meterUnits, alternateSizeUnits);
	}
	
	public static String meterOrbitToString(double value) {
		return unitToString(value, meterUnits, alternate1OrbitUnits, alternate2OrbitUnits);
	}
	
	public static String meterDistanceToString(double value) {
		return unitToString(value, meterDistanceUnits);
	}
	
	public static String secondsToString (double value) {
		return unitToString(value, secondUnits);
	}

	public static String kilogramsToString (double value) {
		return unitToString(value, kilogramUnits, alternateKilogramUnits);
	}

	public static String kelvinToString(double value) {
		return toString(value) + " K  (" + toString(value - 273.16) + " C)";
	}

	public static String pascalToString(double value) {
		return toString(value) + " Pa  (" + toString(value / Units.EARTH_ATMOSPHERE_PRESSURE) + " bar)";
	}
	
	public static String volumeToString(double value) {
		return toString(value) + " m^3";
	}

	public static String densityToString(double value) {
		return toString(value) + " kg/m^3";
	}
	
	public static String newtonGravityToString(double value) {
		return unitToString(value, true, newtonUnits, alternateNewtonGravityUnits);
	}
	
	public static String metersPerSecondToString (double value) {
		return unitToString(value, speedUnits);
	}
	
	public static double gravity(double mass, double distance) {
		return mass * Units.GRAVITATIONAL_CONSTANT / (distance * distance);
	}
	
	public static double escapeVelocity(double mass, double distance) {
		return Math.sqrt(2 * Units.GRAVITATIONAL_CONSTANT * mass / distance);
	}

	public static String percentToString(double value) {
		return toString(value * 100) + "%";
	}

	public static String moneyToString(double value) {
		return toString(value) + " $";
	}

	public static String unitToString(double value, Unit units[], Unit[]... alternateUnits) {
		return unitToString(value, false, units, alternateUnits);
	}
	
	public static String unitToString(double value, boolean showLast, Unit units[], Unit[]... alternateUnits) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(unitToString(value, true, units));

		boolean shown = false;
		for (Unit[] alternate : alternateUnits) {
			String earthString = unitToString(value, showLast, alternate);
			if (earthString != null) {
				if (shown) {
					stringBuilder.append(", ");
				} else {
					stringBuilder.append("  (");
				}
				stringBuilder.append(earthString);
				shown = true;
			}
		}
		if (shown) {
			stringBuilder.append(")");
		}
		
		return stringBuilder.toString();
	}

	private static String unitToString(double value, boolean showLast, Unit units[]) {
		for (int i = 0; i < units.length; i++) {
			Unit unit = units[i];
			if ((showLast && i == units.length-1) || Math.abs(value) > 0.9 * unit.value) {
				return toString(value / unit.value) + " " + unit.name;
			}
		}
		return null;
	}
	
	public static double celsiusToKelvin(double celsius) {
		return celsius + CELSIUS_BASE;
	}
	
	private static class Unit {
		double value;
		String name;
		
		public Unit(double value, String unit) {
			this.value = value;
			this.name = unit;
		}
	}

	public static double volumeSphere(double radius) {
		return radius * radius * radius * Math.PI * 4 / 3;
	}
	
	public static void millisToPlanetTime(PlanetTime planetTime, long millis, long planetRevolutionMillis) {
		//long days = millis / planetRevolutionMillis;
		long dayMillis = millis % planetRevolutionMillis;
		planetTime.dayFraction = ((double) dayMillis) / planetRevolutionMillis;
		
		long remaining = dayMillis;
		long h = remaining / 3600 / 1000;
		remaining -= h * 3600 * 1000;
		long m = remaining / 60 / 1000;
		remaining -= m * 60 * 1000;
		long s = remaining / 1000;
		remaining -= s * 1000;
		
		planetTime.hours = (int) h;
		planetTime.minutes = (int) m;
		planetTime.seconds = (int) s;
		planetTime.milliseconds = (int) remaining;
	}
	
	public static class PlanetTime {
		public double dayFraction;
		public int hours;
		public int minutes;
		public int seconds;
		public int milliseconds;
	}

	public static StringBuilder toString(StringBuilder stringBuilder, int value) {
		return toString(stringBuilder, value, ' ', 0);
	}
	
	public static StringBuilder toString(StringBuilder stringBuilder, int value, char pad, int length) {
		stringBuilder.setLength(0);
		String str = String.valueOf(value);
		
		for (int i = 0; i < length - str.length(); i++) {
			stringBuilder.append(pad);
		}
		stringBuilder.append(value);
		
		return stringBuilder;
	}

	public static float toRenderUnit(double meters) {
		return (float) (meters / Units.EARTH_RADIUS);
	}

	public static double toMeter(float renderUnits) {
		return renderUnits * Units.EARTH_RADIUS;
	}
}
