package ch.obermuhlner.planetgen.height;
import java.util.Random;

import ch.obermuhlner.planetgen.noise.SimplexNoise;

public class FractalNoise {

    private SimplexNoise[] octaves;

    private double persistence;

	public FractalNoise(double largestFeature, double persistence, Random random){
        this.persistence = persistence;

        int numberOfOctaves = (int)Math.ceil(Math.log10(largestFeature)/Math.log10(2));

        octaves = new SimplexNoise[numberOfOctaves];

        for(int i=0; i<numberOfOctaves; i++){
            octaves[i] = new SimplexNoise(random);
        }
    }

    public double getNoise(double x, double y){

        double result=0;

        double frequency = 1.0;
        double amplitude = Math.pow(persistence, octaves.length);
        for(int i=0; i<octaves.length; i++){

            double signal = octaves[i].noise(x/frequency, y/frequency);
			result += signal * amplitude;
            
            frequency *= 2.0;
            amplitude /= persistence;
        }

        return result;

    }

    public double getNoise(double x, double y, double z){

        double result=0;

        double frequency = 1.0;
        double amplitude = Math.pow(persistence, octaves.length);
        for(int i=0; i<octaves.length; i++){
          result += octaves[i].noise(x/frequency, y/frequency, z/frequency) * amplitude;

          frequency *= 2.0;
          amplitude /= persistence;
        }

        return result;
    }
} 