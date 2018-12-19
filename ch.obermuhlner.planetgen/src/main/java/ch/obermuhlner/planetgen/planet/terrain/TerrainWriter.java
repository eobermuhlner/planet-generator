package ch.obermuhlner.planetgen.planet.terrain;

public interface TerrainWriter {
    int getWidth();
    int getHeight();
    void setValue(int x, int y, double value);
}
