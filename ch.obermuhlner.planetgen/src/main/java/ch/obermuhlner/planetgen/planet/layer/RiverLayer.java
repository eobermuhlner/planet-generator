package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.LayerType;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Random;

import java.util.ArrayList;
import java.util.List;

public class RiverLayer implements Layer {

    private final long seed;
    private final PlanetGenerationContext heightContext;

    private List<River> rivers;

    public RiverLayer(long seed) {
        this.seed = seed;

        heightContext = new PlanetGenerationContext();
        heightContext.layerTypes.add(LayerType.GROUND);
        heightContext.layerTypes.add(LayerType.CRATERS);
        heightContext.accuracy = 100;
    }

    @Override
    public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
        if (planet.planetData.riverDensity <= 0.0) {
            return;
        }

        synchronized (this) {
            if (rivers == null) {
                rivers = createRivers(planet);
                System.out.println(rivers);
            }
        }

        double height = planetPoint.groundHeight;
        if (height > 0) {
            Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planet.planetData.radius);

            boolean isWater = false;
            double maxRiverDiameter = 8_000;
            double maxRiverBedDiameter = 1_000;
            double maxRiverInfluenceDiameter = 100_000;
            double maxRiverDepth = 50;
            double maxRiverLength = 1_000_000;
            for (River river : rivers) {
                MathUtil.ClosestPoint closestPoint = MathUtil.closestPoint(river.start.toVector2(), river.end.toVector2(), Vector2.of(latitude, longitude));
                Vector3 riverStartCartesian = Vector3.ofPolar(river.start.latitude, river.start.longitude, planet.planetData.radius);
                Vector3 riverPointCartesian = Vector3.ofPolar(closestPoint.closest.x, closestPoint.closest.y, planet.planetData.radius);

                double distance = pointCartesian.subtract(riverPointCartesian).getLength();
                double riverLengthAtPoint = riverStartCartesian.subtract(riverPointCartesian).getLength();
                double riverValue = MathUtil.smoothstep(0.0, 1.0, riverLengthAtPoint / maxRiverLength);
                double riverBedDiameter = riverValue * maxRiverBedDiameter;
                double riverInfluenceDiameter = riverValue * maxRiverInfluenceDiameter;
                double riverDiameter = Math.max(riverBedDiameter, riverValue * maxRiverDiameter);

                isWater |= distance < riverDiameter;
                double riverBedValue = 1.0 - MathUtil.smoothstep(0.2, 1.0, distance / riverBedDiameter);
                double riverInfluenceValue = 1.0 - MathUtil.smoothstep(0.2, 1.0, distance / riverInfluenceDiameter);

                double erosion = riverValue * riverInfluenceValue * height + riverValue * riverBedValue * maxRiverDepth;
                height -= erosion;
                planetPoint.debug += riverInfluenceValue;
            }

            planetPoint.groundHeight = height;
            planetPoint.isWater = planetPoint.height <= 0 || isWater;
            planetPoint.height = planetPoint.groundHeight;
        }
    }

    private List<River> createRivers(Planet planet) {
        List<River> result = new ArrayList<>();

        Random random = new Random(seed);

        int nRivers = (int) (1000 * planet.planetData.riverDensity);
        for (int riverIndex = 0; riverIndex < nRivers; riverIndex++) {
            River river = createRiver(planet, random);

            if (river != null) {
                MathUtil.Intersection closestIntersection = null;
                River closestRiver = null;
                for (River oldRiver : result) {
                    MathUtil.Intersection intersection = MathUtil.lineIntersection(river.start.toVector2(), river.end.toVector2(), oldRiver.start.toVector2(), oldRiver.end.toVector2(), 0.3);
                    if (intersection != null) {
                        if (closestIntersection == null || intersection.t1 < closestIntersection.t1) {
                            closestRiver = oldRiver;
                            closestIntersection = intersection;
                        }
                    }
                }
                if (closestIntersection != null) {
                    RiverPoint riverCrossing = closestRiver.getRiverPointAt(0.5);
                    river = new River(river.start, riverCrossing);
                    //river = null;
                }

                if (river != null) {
                    result.add(river);
                }
            }
        }

        return result;
    }

    private River createRiver(Planet planet, Random random) {
        RiverPoint start = null;
        RiverPoint end = null;
        double startHeight = 0;

        int nStartPoints = 10;
        for (int pointIndex = 0; pointIndex < nStartPoints; pointIndex++) {
            double latitude = random.nextDouble(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE);
            double longitude = random.nextDouble(Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE);
            double height = planet.getPlanetPoint(latitude, longitude, heightContext).groundHeight;
            if (height > 0) {
                if (start == null || height > startHeight) {
                    start = new RiverPoint(latitude, longitude);
                    startHeight = height;
                }
            }
        }
        if (start != null) {
            int nEndPoints = 100;
            double bestDistanceSquared = Double.MAX_VALUE;
            for (int pointIndex = 0; pointIndex < nEndPoints; pointIndex++) {
                double latitude = random.nextDouble(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE);
                double longitude = random.nextDouble(Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE);
                double height = planet.getPlanetPoint(latitude, longitude, heightContext).groundHeight;
                if (height <= 0) {
                    RiverPoint point = new RiverPoint(latitude, longitude);
                    double distanceSquared = start.distanceSquared(point);
                    if (distanceSquared < bestDistanceSquared) {
                        end = point;
                        bestDistanceSquared = distanceSquared;
                    }
                }
            }
        }

        if (start == null || end == null) {
            return null;
        }

        return new River(start, end);
    }

    private static class RiverPoint {
        double latitude;
        double longitude;

        public RiverPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double distance(RiverPoint other) {
            // TODO real impl - this is just cartesian!
            return Math.sqrt(distanceSquared(other));
        }

        public double distanceSquared(RiverPoint other) {
            // TODO real impl - this is just cartesian!
            double dx = other.latitude - latitude;
            double dy = other.longitude - longitude;
            return dx*dx + dy*dy;
        }

        Vector2 toVector2() {
            return Vector2.of(latitude, longitude);
        }

        @Override
        public String toString() {
            return "RiverPoint{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }

    private static class River {
        RiverPoint start;
        RiverPoint end;

        double length;

        public River(RiverPoint start, RiverPoint end) {
            this.start = start;
            this.end = end;

            length = start.distance(end);
        }

        public double distanceToRiver(double latitude, double longitude) {
            double nom = (end.longitude-start.longitude)*latitude - (end.latitude - start.longitude)*longitude + end.latitude*start.longitude + end.longitude*start.latitude;
            return Math.abs(nom) / length;
        }

        public RiverPoint getRiverPointAt(double value) {
            double pointLatitude = (end.latitude - start.latitude) * value + start.latitude;
            double pointLongitude = (end.longitude - start.longitude) * value + start.longitude;
            return new RiverPoint(pointLatitude, pointLongitude);
        }

        @Override
        public String toString() {
            return "River{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
