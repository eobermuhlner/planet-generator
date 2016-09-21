package ch.obermuhlner.planetgen.height;
import java.util.Random;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.noise.SimplexNoise;

public class FractalNoise {

	private NoiseFunction noiseFunction;
	private AmplitudeFunction amplitudeFunction;
	
    private SimplexNoise[] octaves;

	public FractalNoise(double largestFeature, NoiseFunction noiseFunction, AmplitudeFunction amplitudeFunction, Random random){
		this.noiseFunction = noiseFunction;
		this.amplitudeFunction = amplitudeFunction;
		
		int numberOfOctaves = (int)Math.ceil(Math.log10(largestFeature)/Math.log10(2));

        octaves = new SimplexNoise[numberOfOctaves];
        for(int i=0; i<numberOfOctaves; i++){
            octaves[i] = new SimplexNoise(random);
        }
    }

    public double getNoise(double x, double y){

        double result = 0;

        double frequency = Math.pow(2, octaves.length);
        double amplitude = 1.0;
        for(int i=0; i<octaves.length; i++){

            double noise = octaves[i].noise(x/frequency, y/frequency);
            noise = transformNoise(noise);
			result += noise * amplitude;
            
            frequency /= 2.0;
            amplitude = amplitudeFunction.nextAmplitude(amplitude, noise);
        }

        return result;
    }

	public double getNoise(double x, double y, double z){

        double result = 0;

        double frequency = Math.pow(2, octaves.length);
        double amplitude = 1.0;
        for(int i=0; i<octaves.length; i++){
          double noise = octaves[i].noise(x/frequency, y/frequency, z/frequency);
          noise = noiseFunction.transformNoise(noise);
          result += noise * amplitude;

          frequency *= 2.0;
          amplitude = amplitudeFunction.nextAmplitude(amplitude, noise);
        }

        return result;
    }
	
    private double transformNoise(double noise) {
    	//return noise;
    	//return noise > 0 ? Math.pow(noise, 2.0) : noise;
    	//return noise > 0 ? Math.sqrt(noise) : noise;
    	//return Math.sqrt(Math.abs(noise)) * 2.0 - 1.0;
		//return (1.0 - Math.abs(noise)) * 2.0 - 1.0; // ridge
    	return noise > 0 ? 1.0 - Math.abs(noise * noise - 0.5) * 2.0 : noise; // continental ridge
    	//return noise > 0 ? 1.0 - Math.abs(noise) : noise; // Picasso
	}

	public static interface AmplitudeFunction {
		double nextAmplitude(double amplitude, double noise);
	}
	
	public static class PersistenceAmplitude implements AmplitudeFunction {
		private double persistence;
		public PersistenceAmplitude(double persistence) {
			this.persistence = persistence;
		}
		@Override
		public double nextAmplitude(double amplitude, double noise) {
			return amplitude * persistence;
		}
	}
	
	public static class WeightedAmplitude implements AmplitudeFunction {
		@Override
		public double nextAmplitude(double amplitude, double noise) {
			double signal = noise * 0.5 + 0.5;
			return amplitude * signal;
		}
	}
	
	public static interface NoiseFunction {
		double transformNoise(double noise);
	}
	
	public static class LinearNoise implements NoiseFunction {
		@Override
		public double transformNoise(double noise) {
			return noise;
		}
	}

	public static class PositiveNegativeNoise implements NoiseFunction {
		
		private NoiseFunction positiveNoise;
		private NoiseFunction negativeNoise;

		public PositiveNegativeNoise(NoiseFunction positiveNoise, NoiseFunction negativeNoise) {
			this.positiveNoise = positiveNoise;
			this.negativeNoise = negativeNoise;
		}
		
		@Override
		public double transformNoise(double noise) {
			if (noise >= 0) {
				return positiveNoise.transformNoise(noise);
			} else {
				return negativeNoise.transformNoise(noise);				
			}
		}
	}

	public static class PowerNoise implements NoiseFunction {
		private double power;
		
		public PowerNoise(double power) {
			this.power = power;
		}
		
		@Override
		public double transformNoise(double noise) {
	    	return Math.pow(noise, power);
		}
	}

	public static class RidgeNoise implements NoiseFunction {
		@Override
		public double transformNoise(double noise) {
			return 1.0 - Math.abs(noise);
		}
	}
	
	public static class TransformRangeNoise implements NoiseFunction {
		private double fromMin;
		private double fromMax;
		private double toMin;
		private double toMax;

		public TransformRangeNoise(double fromMin, double fromMax, double toMin, double toMax) {
			this.fromMin = fromMin;
			this.fromMax = fromMax;
			this.toMin = toMin;
			this.toMax = toMax;
		}

		@Override
		public double transformNoise(double noise) {
			return MathUtil.transform(fromMin, fromMax, toMin, toMax, noise);
		}
	}

	public static class MultipleNoise implements NoiseFunction {
		private NoiseFunction[] noiseFunctions;

		public MultipleNoise(NoiseFunction... noiseFunctions) {
			this.noiseFunctions = noiseFunctions;
		}
		
		@Override
		public double transformNoise(double noise) {
			for (NoiseFunction noiseFunction : noiseFunctions) {
				noise = noiseFunction.transformNoise(noise);
			}
			return noise;
		}
	}
	
}