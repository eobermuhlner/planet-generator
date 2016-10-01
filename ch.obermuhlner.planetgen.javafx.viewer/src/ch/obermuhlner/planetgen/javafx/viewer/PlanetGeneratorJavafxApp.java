package ch.obermuhlner.planetgen.javafx.viewer;

import java.text.DecimalFormat;
import java.util.Random;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.TextureType;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetGeneratorJavafxApp extends Application {

	private static final int ZOOM_IMAGE_SIZE = 128;

	private static final int TEXTURE_IMAGE_WIDTH = 1024;
	private static final int TEXTURE_IMAGE_HEIGHT = TEXTURE_IMAGE_WIDTH / 2;

	private static final int HEIGHTMAP_HEIGHT = 256;

	private static final int MAP_WIDTH = 1024;
	
	private ImageView diffuseImageView;
	private ImageView normalImageView;
	private ImageView luminousImageView;
	private ImageView thermalImageView;
	private ImageView thermalAverageImageView;

	private PhongMaterial material;

	private DoubleProperty latitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty longitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty heightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty iceHeightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty snowHeightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0);
	private DoubleProperty renderMillisecondsProperty = new SimpleDoubleProperty(0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(50);
	
	private Planet planet;
	
	private ImageView zoomDiffuseImageView;
	private ImageView zoomNormalImageView;
	private ImageView zoomLuminousImageView;
	private ImageView zoomThermalImageView;
	private double zoomLongitudeDegrees;
	private double zoomLatitudeDegrees;
	private double zoomLatitudeSize;
	private double zoomLongitudeSize;

	private Canvas zoomHeightMapCanvas;

	private Canvas heightMapCanvas;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Random Planet Generator");
        Group root = new Group();
        Scene scene = new Scene(root);
        
        BorderPane mainBorderPane = new BorderPane();
        root.getChildren().add(mainBorderPane);
        
        // view pane
        BorderPane viewBorderPane = new BorderPane();
        mainBorderPane.setCenter(viewBorderPane);
        
        // info pane
        GridPane infoGridPane = new GridPane();
        viewBorderPane.setRight(infoGridPane);
        infoGridPane.setHgap(4);
        infoGridPane.setVgap(4);
        BorderPane.setMargin(infoGridPane, new Insets(4));
        {
        	int rowIndex = 0;
        	addText(infoGridPane, rowIndex++, "Latitude", latitudeProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Longitude", longitudeProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Height [m]", heightProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Ice Height [m]", iceHeightProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Snow Height [m]", snowHeightProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Temperature [K]", temperatureProperty, "##0.000");
        	addText(infoGridPane, rowIndex++, "Render Time [ms]", renderMillisecondsProperty, "##0.000");

        	addSlider(infoGridPane, rowIndex++, "Zoom", zoomProperty, 20, 1000, 50);
        	zoomProperty.addListener((source, oldValue, newValue) -> updateZoomImages(zoomLatitudeDegrees, zoomLongitudeDegrees));
        	
        	zoomDiffuseImageView = new ImageView();
        	infoGridPane.add(zoomDiffuseImageView, 0, rowIndex, 1, 1);
        	setDragZoomMapEvents(zoomDiffuseImageView);

        	zoomNormalImageView = new ImageView();
        	infoGridPane.add(zoomNormalImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomNormalImageView);

        	zoomLuminousImageView = new ImageView();
        	infoGridPane.add(zoomLuminousImageView, 0, rowIndex, 1, 1);
        	setDragZoomMapEvents(zoomLuminousImageView);

        	zoomThermalImageView = new ImageView();
        	infoGridPane.add(zoomThermalImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomThermalImageView);

        	zoomHeightMapCanvas = new Canvas(ZOOM_IMAGE_SIZE, HEIGHTMAP_HEIGHT);
        	infoGridPane.add(zoomHeightMapCanvas, 0, rowIndex++, 1, 1);
        }
        
        // tab pane
        VBox mapBox = new VBox();
        mapBox.setSpacing(4);
        viewBorderPane.setCenter(mapBox);
        TabPane tabPane = new TabPane();
        mapBox.getChildren().add(tabPane);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        heightMapCanvas = new Canvas(MAP_WIDTH, HEIGHTMAP_HEIGHT);
        mapBox.getChildren().add(heightMapCanvas);
        
        // 2D diffuse texture
        diffuseImageView = addTabImageView(tabPane, "2D Color");

        // 2D normal texture
        normalImageView = addTabImageView(tabPane, "2D Normal");

        // 2D luminous texture
        luminousImageView = addTabImageView(tabPane, "2D Luminous");

        // 2D thermal texture
        thermalImageView = addTabImageView(tabPane, "2D Thermal");

        // 2D thermal average texture
        thermalAverageImageView = addTabImageView(tabPane, "2D Thermal Average");

        // 3D planet
    	StackPane node3dContainer = new StackPane();
    	tabPane.getTabs().add(new Tab("3D", node3dContainer));
    	Group world = new Group();
		material = new PhongMaterial(Color.WHITE);
    	Node node3d = createNode3D(node3dContainer, world, material);
    	node3dContainer.getChildren().add(node3d);
    	
        // editor border pane
        BorderPane editBorderPane = new BorderPane();
        viewBorderPane.setLeft(editBorderPane);
        
        // buttons in editor border pane
        VBox buttonBox = new VBox();
        buttonBox.setSpacing(4);
        editBorderPane.setTop(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(4));
        
        Button randomPlanetButton = new Button("Random Planet");
        buttonBox.getChildren().add(randomPlanetButton);
        randomPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
            updateRandomPlanet();
        });

        // initial run
        updateRandomPlanet();
        
    	primaryStage.setScene(scene);
        primaryStage.show();
	}

	private ImageView addTabImageView(TabPane tabPane, String name) {
		ImageView imageView = new ImageView();
        imageView.setFitWidth(MAP_WIDTH);
        imageView.setPreserveRatio(true);
        tabPane.getTabs().add(new Tab(name, imageView));
        setInfoAndZoomEvents(imageView);
        
        return imageView;
	}

	private void updateRandomPlanet() {
		long startNanoTime = System.nanoTime();
    	planet = createRandomPlanet();
    	long endNanoTime = System.nanoTime();
    	renderMillisecondsProperty.set((endNanoTime - startNanoTime) / 1000000.0);
    	
    	updateZoomImages(0, 180);
	}
	
	private double lastMouseDragX;
	private double lastMouseDragY;
	private void setDragZoomMapEvents(ImageView imageView) {
		imageView.setOnMousePressed(event -> {
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
		});
		imageView.setOnMouseDragged(event -> {
			double deltaX = event.getX() - lastMouseDragX;
			double deltaY = event.getY() - lastMouseDragY;
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
			
			double longitudeDegrees = toLongitude(-deltaX, imageView.getImage(), zoomLongitudeSize * imageView.getImage().getWidth());
			double latitudeDegrees = toLatitude(deltaY, imageView.getImage(), zoomLatitudeSize * imageView.getImage().getHeight());
			
			updateZoomImages(zoomLatitudeDegrees + latitudeDegrees, zoomLongitudeDegrees + longitudeDegrees);
		});
	}

	private void setInfoAndZoomEvents(ImageView imageView) {
		imageView.setOnMouseClicked(event -> {
        	imageInfoAndZoomMouseEvent(event, imageView);
        });
		imageView.setOnMouseDragged(event -> {
        	imageInfoAndZoomMouseEvent(event, imageView);
        });
	}

	private void imageInfoAndZoomMouseEvent(MouseEvent event, ImageView imageView) {
		double longitudeDegrees = toLongitude(event.getX(), imageView.getImage(), 360);
		double latitudeDegrees = toLatitude(imageView.getImage().getHeight() - event.getY(), imageView.getImage(), 180) - 90;
		
		updateZoomImages(latitudeDegrees, longitudeDegrees);
	}
	
	private void updateZoomImages(double latitudeDegrees, double longitudeDegrees) {
		zoomLongitudeDegrees = longitudeDegrees;
		zoomLatitudeDegrees = latitudeDegrees;
		
		longitudeProperty.set(longitudeDegrees);
		latitudeProperty.set(latitudeDegrees);
		
		PlanetGenerationContext context = planet.createDefaultContext();
		context.enabledTextureTypes.add(TextureType.DIFFUSE);
		context.enabledTextureTypes.add(TextureType.NORMAL);
		context.enabledTextureTypes.add(TextureType.LUMINOUS);
		context.enabledTextureTypes.add(TextureType.THERMAL);
		JavafxPlanetTextures planetTextures = new JavafxPlanetTextures(ZOOM_IMAGE_SIZE, ZOOM_IMAGE_SIZE, context);
		
		double latitudeRadians = Math.toRadians(180) - Math.toRadians(latitudeDegrees + 90);
		double longitudeRadians = Math.toRadians(longitudeDegrees);
		PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
		heightProperty.set(planetPoint.groundHeight);
		iceHeightProperty.set(planetPoint.iceHeight);
		snowHeightProperty.set(planetPoint.snowHeight);
		temperatureProperty.set(planetPoint.temperature);
		
		zoomLatitudeSize = Planet.RANGE_LATITUDE / zoomProperty.get() * 2;
		zoomLongitudeSize = Planet.RANGE_LONGITUDE / zoomProperty.get();
		planet.getTextures(
				latitudeRadians - zoomLatitudeSize,
				latitudeRadians + zoomLatitudeSize,
				longitudeRadians - zoomLongitudeSize,
				longitudeRadians + zoomLongitudeSize,
				ZOOM_IMAGE_SIZE, ZOOM_IMAGE_SIZE,
				context,
				planetTextures);
		zoomDiffuseImageView.setImage(planetTextures.getImage(TextureType.DIFFUSE));
		zoomNormalImageView.setImage(planetTextures.getImage(TextureType.NORMAL));
		zoomLuminousImageView.setImage(planetTextures.getImage(TextureType.LUMINOUS));
		zoomThermalImageView.setImage(planetTextures.getImage(TextureType.THERMAL));

		drawHeightMap(heightMapCanvas, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, latitudeRadians);
		drawHeightMap(zoomHeightMapCanvas, longitudeRadians - zoomLongitudeSize, longitudeRadians + zoomLongitudeSize, latitudeRadians);
	}

	private void drawHeightMap(Canvas canvas, double fromLongitude, double toLongitude, double latitude) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		int canvasWidth = (int) (canvas.getWidth() + 0.5);
		int canvasHeight = (int) canvas.getHeight();

		PlanetGenerationContext context = new PlanetGenerationContext();
		context.accuracy = 1.0;
		
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvasWidth, canvasHeight);

		double stepLongitude = (toLongitude - fromLongitude) / canvasWidth;
		
		double heightRange = planet.planetData.maxHeight * 2 - planet.planetData.minHeight * 2;
		double heightFactor = canvasHeight / heightRange;
		for (int x = 0; x < canvasWidth; x++) {
			double longitude = fromLongitude + stepLongitude * x;
			PlanetPoint point = planet.getPlanetPoint(latitude, longitude, context);

			double groundY = (point.groundHeight - planet.planetData.minHeight) * heightFactor;
			gc.setStroke(ColorUtil.toJavafxColor(point.groundColor));
			gc.strokeLine(x, canvasHeight, x, canvasHeight - groundY);
			double lastY = groundY;
			
			if (point.groundHeight <= 0) {
				double oceanY = (0 - planet.planetData.minHeight) * heightFactor;
				gc.setStroke(ColorUtil.toJavafxColor(point.oceanColor));
				gc.strokeLine(x, canvasHeight - lastY, x, canvasHeight - oceanY);
				lastY = oceanY;
			}
			
			if (point.plantColor != null) {
				gc.setStroke(ColorUtil.toJavafxColor(point.plantColor));
				gc.strokeLine(x, canvasHeight - lastY, x, canvasHeight - lastY);
			}
			
			if (point.iceHeight > 0) {
				double iceY = (point.height - planet.planetData.minHeight) * heightFactor;
				gc.setStroke(ColorUtil.toJavafxColor(point.iceColor));
				gc.strokeLine(x, canvasHeight - lastY, x, canvasHeight - iceY);
			}
		}
	}

	private double toLongitude(double x, Image image, double degrees) {
		if (image == null) {
			return 0;
		}
		
		return x / image.getWidth() * degrees;
	}

	private double toLatitude(double y, Image image, double degrees) {
		if (image == null) {
			return 0;
		}
		
		return y / image.getHeight() * degrees;
	}

	private Text addText(GridPane gridPane, int rowIndex, String label, DoubleProperty doubleProperty, String formatPattern) {
    	gridPane.add(new Text(label), 0, rowIndex);
    	Text valueText = new Text();
		Bindings.bindBidirectional(valueText.textProperty(), doubleProperty, new DecimalFormat(formatPattern));
    	gridPane.add(valueText, 1, rowIndex);
    	return valueText;
	}

	private Slider addSlider(GridPane gridPane, int rowIndex, String label, DoubleProperty doubleProperty, double min, double max, double value) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        Slider valueSlider = new Slider(min, max, value);
        Bindings.bindBidirectional(doubleProperty, valueSlider.valueProperty());
		gridPane.add(valueSlider, 1, rowIndex);
		return valueSlider;
	}

	private Node createNode3D(Region container, Group world, PhongMaterial material) {
        Sphere sphere = new Sphere();
		sphere.setMaterial(material);
        world.getChildren().add(sphere);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sphere.setRotate(sphere.getRotate() + 0.01);
			}
		}));
		timeline.playFromStart();
        
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
        		new Rotate(-20, Rotate.Y_AXIS),
        		new Rotate(-20, Rotate.X_AXIS),
        		new Translate(0, 0, -5)
        		);
        world.getChildren().add(camera);

        SubScene subScene = new SubScene(world, 800, 600, false, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        subScene.heightProperty().bind(container.heightProperty());
        subScene.widthProperty().bind(container.widthProperty());
        
        return subScene;
	}

	private Planet createRandomPlanet() {
		Random random = new Random();
		
		PlanetGenerator planetGenerator = new PlanetGenerator();
		Planet planet = planetGenerator.createPlanet(random);
		
		PlanetGenerationContext context = planet.createDefaultContext();
		context.enabledTextureTypes.add(TextureType.DIFFUSE);
		context.enabledTextureTypes.add(TextureType.NORMAL);
		context.enabledTextureTypes.add(TextureType.LUMINOUS);
		context.enabledTextureTypes.add(TextureType.THERMAL);
		context.enabledTextureTypes.add(TextureType.THERMAL_AVERAGE);
		JavafxPlanetTextures planetTextures = new JavafxPlanetTextures(TEXTURE_IMAGE_WIDTH, TEXTURE_IMAGE_HEIGHT, context);
		planet.getTextures(TEXTURE_IMAGE_WIDTH, TEXTURE_IMAGE_HEIGHT, context, planetTextures);
		
		Image diffuseImage = planetTextures.getImage(TextureType.DIFFUSE);
		Image normalImage = planetTextures.getImage(TextureType.NORMAL);
		Image luminousImage = planetTextures.getImage(TextureType.LUMINOUS);
		Image thermalImage = planetTextures.getImage(TextureType.THERMAL);
		Image thermalAverageImage = planetTextures.getImage(TextureType.THERMAL_AVERAGE);
		diffuseImageView.setImage(diffuseImage);
		normalImageView.setImage(normalImage);
		luminousImageView.setImage(luminousImage);
		thermalImageView.setImage(thermalImage);
		thermalAverageImageView.setImage(thermalAverageImage);
		
		material.setDiffuseMap(diffuseImage);
		material.setBumpMap(normalImage);
		material.setSelfIlluminationMap(luminousImage); // TODO show only in dark side - but javafx cannot do that
		
		return planet;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
