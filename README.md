# planet-generator

Java based random generator for planets.

The generator is pure Java without any external dependencies.

The viewer is a separate project that uses JavaFX.

## Example Code

The first step to generate a planet is to define the unique random seed for the planet and
generate the basic planet data from it.

```java
        PlanetGenerator planetGenerator = new PlanetGenerator();

        // unique seed for the planet
        long[] seed = new long[] { 123L };

        // generate planet data for unique seed
        PlanetData planetData = planetGenerator.createPlanetData(seed);

        // random generated planet data
        System.out.println("radius : " + planetData.radius + " m");
        System.out.println("revolutionTime : " + planetData.revolutionTime + " s");
        System.out.println("orbitTime : " + planetData.orbitTime + " s");

        // modify generated planet data if you need to fulfill special constraints
        planetData.baseTemperature = 270; // Celsius - expect cold tundra at the equator, large polar caps

        // create planet according to planet data constraints
        Planet planet = planetGenerator.createPlanet(planetData);
```

With the planet data generated we can now generate specific information for any point of the surface of the planet.

This is useful to know the conditions at the location of the player in a game.

```java
        // specify which layers and what accuracy you need (default has all layers and good enough accuracy)
        PlanetGenerationContext context = planet.createDefaultContext();

        // generate planet at one specific point
        double latitudeRadians = Math.toRadians(180) - Math.toRadians(47.2266 + 90);
        double longitudeRadians = Math.toRadians(8.8184);
        PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
        System.out.println("height : " + planetPoint.height + " m");
        System.out.println("temperature : " + planetPoint.temperature + " K");
        System.out.println("precipitation : " + planetPoint.precipitation);
        System.out.println("color : " + planetPoint.color);
```

For rendering an entire planet we can create the necessary textures into images.

The following example creates all texture types that the framework knows about.

```java
        PlanetGenerationContext context = planet.createDefaultContext();
        context.textureTypes.addAll(Arrays.asList(TextureType.values()));

        Map<TextureType, TextureWriter<BufferedImage>> textures = planet.getTextures(1024, 512, context, (width, height, textureType) -> new BufferedImageTextureWriter(width, height));

        try {
            for (TextureType textureType : TextureType.values()) {
                String filename = textureType.name().toLowerCase() + ".png";
                BufferedImage image = textures.get(textureType).getTexture();
                ImageIO.write(image, "png", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
```

![Surface Map](ch.obermuhlner.planetgen.docs/images/seed123/diffuse.png)

![Normal Map](ch.obermuhlner.planetgen.docs/images/seed123/normal.png)

![Average Precipitation Map](ch.obermuhlner.planetgen.docs/images/seed123/precipitation_average.png)

![Temperature Precipitation Map](ch.obermuhlner.planetgen.docs/images/seed123/thermal_average.png)

## Screenshots

For debugging purposes an interactive JavaFX viewer was written that allows to
see and modify many planet data values and
analyze the generated planet.

Interactive textures with point information and zoom.

![Screenshot Viewer](ch.obermuhlner.planetgen.docs/images/planet-generator-1.png?raw=true)

Animated Planet 3D rendering.

![Screenshot Viewer](ch.obermuhlner.planetgen.docs/images/planet-generator-2.png?raw=true)

Interactive animated Terrain 3D rendering with zoom.

![Screenshot Viewer](ch.obermuhlner.planetgen.docs/images/planet-generator-3.png?raw=true)

Visualization of mathematical crater models. 

![Screenshot Viewer](ch.obermuhlner.planetgen.docs/images/planet-generator-4.png?raw=true)
