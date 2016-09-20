package ch.obermuhlner.planetgen.height;
import java.util.Random;

import ch.obermuhlner.planetgen.noise.SimplexNoise;

public class NoiseHeight implements Height {

    private SimplexNoise[] octaves;

    private double persistence;

	public NoiseHeight(double largestFeature, double persistence, Random random){
        this.persistence = persistence;

        int numberOfOctaves = (int)Math.ceil(Math.log10(largestFeature)/Math.log10(2));

        octaves = new SimplexNoise[numberOfOctaves];

        for(int i=0; i<numberOfOctaves; i++){
            octaves[i] = new SimplexNoise(random);
        }
    }


    public double getNoise(double x, double y){

        double result=0;

        for(int i=0; i<octaves.length; i++){
            double frequency = Math.pow(2, i);
            double amplitude = Math.pow(persistence, octaves.length-i);

            result += octaves[i].noise(x/frequency, y/frequency) * amplitude;
        }

        return result;

    }

    public double getNoise(double x, double y, double z){

        double result=0;

        for(int i=0; i<octaves.length; i++){
          double frequency = Math.pow(2, i);
          double amplitude = Math.pow(persistence, octaves.length-i);

          result += octaves[i].noise(x/frequency, y/frequency,z/frequency) * amplitude;
        }

        return result;
    }
    
    @Override
    public double height(double latitude, double longitude, double accuracy) {
    	return getNoise(latitude, longitude) * 0.5 + 0.5;
    }
} 