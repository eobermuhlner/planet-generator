package ch.obermuhlner.planetgen.javafx.viewer;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.TextureType;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.util.Random;
import ch.obermuhlner.util.Tuple2;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetGeneratorJavafxApp extends Application {

	private static final boolean SHOW_DEBUG_VALUE = true;
	
	private static final int ZOOM_IMAGE_SIZE = 128;

	private static final int TEXTURE_IMAGE_WIDTH = 1024;
	private static final int TEXTURE_IMAGE_HEIGHT = TEXTURE_IMAGE_WIDTH / 2;

	private static final int HEIGHTMAP_HEIGHT = 256;

	private static final int MAP_WIDTH = 1024;
	
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
	
	private static final DecimalFormat LONG_FORMAT = new DecimalFormat("##0");
	
	private ImageView diffuseImageView;
	private ImageView normalImageView;
	private ImageView luminousImageView;
	private ImageView thermalImageView;
	private ImageView thermalAverageImageView;
	private ImageView precipitationImageView;
	private ImageView precipitationAverageImageView;

	private PhongMaterial material;
	
	private LongProperty seedProperty = new SimpleLongProperty();
	private DoubleProperty radiusProperty = new SimpleDoubleProperty();
	private DoubleProperty minHeightProperty = new SimpleDoubleProperty();
	private DoubleProperty maxHeightProperty = new SimpleDoubleProperty();
	private BooleanProperty hasOceanProperty = new SimpleBooleanProperty();
	private DoubleProperty craterDensityProperty = new SimpleDoubleProperty();
	private DoubleProperty baseTemperatureProperty = new SimpleDoubleProperty();
	private DoubleProperty seasonalBaseTemperatureVariationProperty = new SimpleDoubleProperty();
	private DoubleProperty dailyBaseTemperatureVariationProperty = new SimpleDoubleProperty();
	private ListProperty<PlantData> plantsProperty = new SimpleListProperty<PlantData>();
	private ListProperty<Crater> cratersProperty = new SimpleListProperty<Crater>();
	private DoubleProperty seasonTemperatureInfluenceToAverageProperty = new SimpleDoubleProperty();
	private DoubleProperty dailyTemperatureInfluenceToAverageProperty = new SimpleDoubleProperty();
	private DoubleProperty dailyTemperatureOceanDelayProperty = new SimpleDoubleProperty();
	private DoubleProperty dailyTemperatureGroundDelayProperty = new SimpleDoubleProperty();
	private DoubleProperty dailyTemperatureOceanFactorProperty = new SimpleDoubleProperty();
	private DoubleProperty seasonProperty = new SimpleDoubleProperty();
	private DoubleProperty dayTimeProperty = new SimpleDoubleProperty();
	
	private DoubleProperty latitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty longitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty heightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty iceHeightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty snowHeightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0);
	private DoubleProperty precipitationAverageProperty = new SimpleDoubleProperty(0);
	private DoubleProperty debugProperty = new SimpleDoubleProperty(0);
	private DoubleProperty renderMillisecondsProperty = new SimpleDoubleProperty(0);
	private DoubleProperty zoomProperty = new SimpleDoubleProperty(50);
	
	private final PlanetGenerator planetGenerator = new PlanetGenerator();

	private Planet planet;
	
	private ImageView zoomDiffuseImageView;
	private ImageView zoomNormalImageView;
	private ImageView zoomLuminousImageView;
	private ImageView zoomThermalImageView;
	private ImageView zoomPrecipitationImageView;
	private double zoomLongitudeDegrees;
	private double zoomLatitudeDegrees;
	private double zoomLatitudeSize;
	private double zoomLongitudeSize;

	private Canvas zoomHeightMapCanvas;

	private Canvas heightMapCanvas;

	private VBox plantGrowthBox;
	private Map<String, Rectangle> mapPlantDataToRectangle = new HashMap<>();
	
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
        	addText(infoGridPane, rowIndex++, "Latitude", latitudeProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Longitude", longitudeProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Height [m]", heightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Ice Height [m]", iceHeightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Snow Height [m]", snowHeightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Temperature [K]", temperatureProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Precipitation Average", precipitationAverageProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Render Time [ms]", renderMillisecondsProperty, DOUBLE_FORMAT);
        	if (SHOW_DEBUG_VALUE) {
        		addText(infoGridPane, rowIndex++, "Debug", debugProperty, DOUBLE_FORMAT);
        	}

        	addSlider(infoGridPane, rowIndex++, "Zoom", zoomProperty, 20, 1000, 50);
        	zoomProperty.addListener((source, oldValue, newValue) -> updateZoomImages(zoomLatitudeDegrees, zoomLongitudeDegrees));
        	
        	zoomDiffuseImageView = new ImageView();
        	infoGridPane.add(zoomDiffuseImageView, 0, rowIndex, 1, 1);
        	setDragZoomMapEvents(zoomDiffuseImageView);

        	zoomThermalImageView = new ImageView();
        	infoGridPane.add(zoomThermalImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomThermalImageView);

        	zoomNormalImageView = new ImageView();
        	infoGridPane.add(zoomNormalImageView, 0, rowIndex, 1, 1);
        	setDragZoomMapEvents(zoomNormalImageView);

        	zoomPrecipitationImageView = new ImageView();
        	infoGridPane.add(zoomPrecipitationImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomPrecipitationImageView);

        	zoomLuminousImageView = new ImageView();
        	infoGridPane.add(zoomLuminousImageView, 0, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomLuminousImageView);

        	zoomHeightMapCanvas = new Canvas(ZOOM_IMAGE_SIZE, HEIGHTMAP_HEIGHT);
        	infoGridPane.add(zoomHeightMapCanvas, 0, rowIndex, 1, 1);
        	
        	plantGrowthBox = new VBox();
        	ScrollPane plantGrowthScrollPane = new ScrollPane(plantGrowthBox);
        	infoGridPane.add(plantGrowthScrollPane, 1, rowIndex++, 1, 1);
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
        diffuseImageView = addTabImageView(tabPane, "Color");

        // 2D normal texture
        normalImageView = addTabImageView(tabPane, "Normal");

        // 2D luminous texture
        luminousImageView = addTabImageView(tabPane, "Luminous");

        // 2D thermal texture
        thermalImageView = addTabImageView(tabPane, "Thermal");

        // 2D thermal average texture
        thermalAverageImageView = addTabImageView(tabPane, "Thermal Average");

        // 2D precipitationtexture
        precipitationImageView = addTabImageView(tabPane, "Precipitation");

        // 2D precipitationtexture
        precipitationAverageImageView = addTabImageView(tabPane, "Precipitation Average");

        // info plants
    	tabPane.getTabs().add(new Tab("Plants", createPlantInfoView()));
        
        // info craters
    	tabPane.getTabs().add(new Tab("Craters", createCratersInfoView()));
        
        // 3D planet
    	StackPane node3dContainer = new StackPane();
    	tabPane.getTabs().add(new Tab("3D", node3dContainer));
    	Group world = new Group();
		material = new PhongMaterial(Color.WHITE);
    	Node node3d = createNode3D(node3dContainer, world, material);
    	node3dContainer.getChildren().add(node3d);
    	
        // editor grid pane
        GridPane editorGridPane = new GridPane();
        viewBorderPane.setLeft(editorGridPane);
        editorGridPane.setHgap(4);
        editorGridPane.setVgap(4);
        BorderPane.setMargin(editorGridPane, new Insets(4));
        {
        	int rowIndex = 0;

        	Button createRandomPlanetButton = new Button("Create Random Planet");
	        editorGridPane.add(createRandomPlanetButton, 0, rowIndex++, 2, 1);
	        createRandomPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            createRandomPlanet();
	        });
	        
        	addTextField(editorGridPane, rowIndex++, "Seed", seedProperty, LONG_FORMAT);

        	Button createPlanetButton = new Button("Create Planet");
	        editorGridPane.add(createPlanetButton, 0, rowIndex++, 2, 1);
	        createPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            updateRandomPlanet(true);
	        });

	        addTextField(editorGridPane, rowIndex++, "Radius [m]", radiusProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Min Height [m]", minHeightProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Max Height [m]", maxHeightProperty, DOUBLE_FORMAT);
	        addCheckBox(editorGridPane, rowIndex++, "Ocean", hasOceanProperty);
        	addSlider(editorGridPane, rowIndex++, "Crater Density", craterDensityProperty, 0.0, 1.0, 0.0);
	        addTextField(editorGridPane, rowIndex++, "Base Temperature [K]", baseTemperatureProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Seasonal Variation [K]", seasonalBaseTemperatureVariationProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Variation [K]", dailyBaseTemperatureVariationProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Season Temperature Influence", seasonTemperatureInfluenceToAverageProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Influence", dailyTemperatureInfluenceToAverageProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ground Delay", dailyTemperatureGroundDelayProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ocean Delay", dailyTemperatureOceanDelayProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ocean Factor ", dailyTemperatureOceanFactorProperty, DOUBLE_FORMAT);
	        
        	addSlider(editorGridPane, rowIndex++, "Season", seasonProperty, 0, 2 * Math.PI, 0);
        	addSlider(editorGridPane, rowIndex++, "Day Time", dayTimeProperty, 0, 2 * Math.PI, 0);
        	
        	Button updatePlanetButton = new Button("Update Planet");
	        editorGridPane.add(updatePlanetButton, 0, rowIndex++, 2, 1);
	        updatePlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            updateRandomPlanet(false);
	        });
	        
        }
        
        // initial run
        createRandomPlanet();
        
    	primaryStage.setScene(scene);
        primaryStage.show();
	}

	private Node createPlantInfoView() {
        BorderPane borderPane = new BorderPane();

        ListView<PlantData> plantsListView = new ListView<PlantData>();
        borderPane.setLeft(plantsListView);
        plantsListView.itemsProperty().bind(plantsProperty);
        plantsProperty.addListener(new ListChangeListener<PlantData>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends PlantData> c) {
				plantsListView.getSelectionModel().selectFirst();
			}
		});
        
        StringProperty plantNameProperty = new SimpleStringProperty();
        DoubleProperty plantTemperatureOptimumProperty = new SimpleDoubleProperty();
        DoubleProperty plantTemperatureMinimumProperty = new SimpleDoubleProperty();
        DoubleProperty plantTemperatureMaximumProperty = new SimpleDoubleProperty();
        DoubleProperty plantPrecipitationOptimumProperty = new SimpleDoubleProperty();
        DoubleProperty plantPrecipitationMinimumProperty = new SimpleDoubleProperty();
        DoubleProperty plantPrecipitationMaximumProperty = new SimpleDoubleProperty();
        
        GridPane plantGridPane = new GridPane();
        borderPane.setRight(plantGridPane);
        plantGridPane.setHgap(4);
        plantGridPane.setVgap(4);
        BorderPane.setMargin(plantGridPane, new Insets(4));

        int rowIndex = 0;
        addText(plantGridPane, rowIndex++, "Name", plantNameProperty);
        Rectangle plantColorRectangle = addNode(plantGridPane, rowIndex++, "Color", new Rectangle(20, 20));
        addText(plantGridPane, rowIndex++, "Temperature Optimum [K]", plantTemperatureOptimumProperty, DOUBLE_FORMAT);
        addText(plantGridPane, rowIndex++, "Temperature Minimum [K]", plantTemperatureMinimumProperty, DOUBLE_FORMAT);
        addText(plantGridPane, rowIndex++, "Temperature Maximum [K]", plantTemperatureMaximumProperty, DOUBLE_FORMAT);
        addText(plantGridPane, rowIndex++, "Precipitation Optimum", plantPrecipitationOptimumProperty, DOUBLE_FORMAT);
        addText(plantGridPane, rowIndex++, "Precipitation Minimum", plantPrecipitationMinimumProperty, DOUBLE_FORMAT);
        addText(plantGridPane, rowIndex++, "Precipitation Maximum", plantPrecipitationMaximumProperty, DOUBLE_FORMAT);
        
        Canvas plantCanvas = new Canvas(400, 400);
        borderPane.setCenter(plantCanvas);
        
        plantsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldPlantData, newPlantData) -> {
    			plantNameProperty.set(newPlantData.name);
    			plantColorRectangle.setFill(ColorUtil.toJavafxColor(newPlantData.color));
    			plantTemperatureOptimumProperty.set(newPlantData.temperatureOptimum);
    			plantTemperatureMinimumProperty.set(newPlantData.temperatureOptimum - newPlantData.temperatureMinusDeviation);
    			plantTemperatureMaximumProperty.set(newPlantData.temperatureOptimum + newPlantData.temperaturePlusDeviation);
    			plantPrecipitationOptimumProperty.set(newPlantData.precipitationOptimum);
    			plantPrecipitationMinimumProperty.set(newPlantData.precipitationOptimum - newPlantData.precipitationMinusDeviation);
    			plantPrecipitationMaximumProperty.set(newPlantData.precipitationOptimum + newPlantData.precipitationPlusDeviation);
    			drawPlantGrowth(plantCanvas, newPlantData);
            });
        
        return borderPane;
	}
	
	private Node createCratersInfoView() {
        BorderPane borderPane = new BorderPane();

        ListView<Crater> cratersListView = new ListView<Crater>();
        borderPane.setLeft(cratersListView);
        cratersListView.itemsProperty().bind(cratersProperty);
        cratersProperty.addListener(new ListChangeListener<Crater>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Crater> c) {
				cratersListView.getSelectionModel().selectFirst();
			}
		});

		LineChart<Number, Number> lineChart = new LineChart<Number, Number>(new NumberAxis(-1.1, 1.1, 0.1), new NumberAxis(-4.0, 2.0, 0.2));
		lineChart.setCreateSymbols(false);
        borderPane.setCenter(lineChart);
		
		ObservableList<Series<Number, Number>> data = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> heightData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Height", heightData));
		ObservableList<Data<Number, Number>> heightNoiseData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Height Noise", heightNoiseData));
		ObservableList<Data<Number, Number>> radialNoiseData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Radial Noise", radialNoiseData));

        cratersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldCrater, newCrater) -> {
        	heightData.clear();
        	heightNoiseData.clear();
        	radialNoiseData.clear();
    		for (double x = -1.1; x <= 1.1; x+=0.005) {
    			heightData.add(new XYChart.Data<>(x, newCrater.heightFunction.calculate(x)));
    			heightNoiseData.add(new XYChart.Data<>(x, newCrater.heightNoiseFunction.calculate(x)));
    			radialNoiseData.add(new XYChart.Data<>(x, newCrater.radialNoiseFunction.calculate(x)));
    		}
        });
		lineChart.dataProperty().set(data);
		
		return borderPane;
	}

	private void drawPlantGrowth(Canvas canvas, PlantData plantData) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		Color plantColor = ColorUtil.toJavafxColor(plantData.color);
		
		for (int y = 0; y < canvas.getHeight(); y++) {
			for (int x = 0; x < canvas.getWidth(); x++) {
				double temperature = y / canvas.getHeight() * 50.0 + 260.0;
				double precipitation = x / canvas.getWidth() * 1.0;
				double plant = plantData.plantGrowth(temperature, precipitation);
				Color color = Color.BEIGE.interpolate(plantColor, plant);
				gc.setFill(color);
				gc.fillRect(x, y, 1, 1);
			}
		}
	}

	private ImageView addTabImageView(TabPane tabPane, String name) {
		ImageView imageView = new ImageView();
        imageView.setFitWidth(MAP_WIDTH);
        imageView.setPreserveRatio(true);
        tabPane.getTabs().add(new Tab(name, imageView));
        setInfoAndZoomEvents(imageView);
        
        return imageView;
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
		context.accuracy = 0.1 / zoomProperty.get();
		context.textureTypes.add(TextureType.DIFFUSE);
		context.textureTypes.add(TextureType.NORMAL);
		context.textureTypes.add(TextureType.LUMINOUS);
		context.textureTypes.add(TextureType.THERMAL);
		context.textureTypes.add(TextureType.PRECIPITATION);
		JavafxPlanetTextures planetTextures = new JavafxPlanetTextures(ZOOM_IMAGE_SIZE, ZOOM_IMAGE_SIZE, context);
		
		double latitudeRadians = Math.toRadians(180) - Math.toRadians(latitudeDegrees + 90);
		double longitudeRadians = Math.toRadians(longitudeDegrees);
		PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
		heightProperty.set(planetPoint.groundHeight);
		iceHeightProperty.set(planetPoint.iceHeight);
		snowHeightProperty.set(planetPoint.snowHeight);
		temperatureProperty.set(planetPoint.temperature);
		precipitationAverageProperty.set(planetPoint.precipitationAverage);
		debugProperty.set(planetPoint.debug);
		
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
		zoomPrecipitationImageView.setImage(planetTextures.getImage(TextureType.PRECIPITATION));

		drawHeightMap(heightMapCanvas, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, latitudeRadians);
		drawHeightMap(zoomHeightMapCanvas, longitudeRadians - zoomLongitudeSize, longitudeRadians + zoomLongitudeSize, latitudeRadians);
		
		for (Tuple2<PlantData, Double> plant : planetPoint.plants) {
			Rectangle plantGrowthBar = mapPlantDataToRectangle.get(plant.getValue1().name);
			if (plantGrowthBar == null) {
				plantGrowthBar = new Rectangle(10, 10, ColorUtil.toJavafxColor(plant.getValue1().color));
				mapPlantDataToRectangle.put(plant.getValue1().name, plantGrowthBar);
				plantGrowthBox.getChildren().add(new Text(plant.getValue1().name));
				plantGrowthBox.getChildren().add(plantGrowthBar);
			};
			plantGrowthBar.setWidth(ZOOM_IMAGE_SIZE * plant.getValue2());
		}
	}

	private void drawHeightMap(Canvas canvas, double fromLongitude, double toLongitude, double latitude) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		int canvasWidth = (int) (canvas.getWidth() + 0.5);
		int canvasHeight = (int) canvas.getHeight();

		PlanetGenerationContext context = planet.createDefaultContext();
		
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
			
			if (planet.planetData.hasOcean) {
				if (point.groundHeight <= 0) {
					double oceanY = (0 - planet.planetData.minHeight) * heightFactor;
					gc.setStroke(ColorUtil.toJavafxColor(point.oceanColor));
					gc.strokeLine(x, canvasHeight - lastY, x, canvasHeight - oceanY);
					lastY = oceanY;
				}
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

	private <T extends Node> T addNode(GridPane gridPane, int rowIndex, String label, T node) {
    	gridPane.add(new Text(label), 0, rowIndex);
    	gridPane.add(node, 1, rowIndex);
    	return node;
	}

	private <T> Text addText(GridPane gridPane, int rowIndex, String label, Property<T> property, Format format) {
    	gridPane.add(new Text(label), 0, rowIndex);
    	Text valueText = new Text();
   		Bindings.bindBidirectional(valueText.textProperty(), property, format);
    	gridPane.add(valueText, 1, rowIndex);
    	return valueText;
	}

	private Text addText(GridPane gridPane, int rowIndex, String label, StringProperty property) {
    	gridPane.add(new Text(label), 0, rowIndex);
    	Text valueText = new Text();
   		Bindings.bindBidirectional(valueText.textProperty(), property);
    	gridPane.add(valueText, 1, rowIndex);
    	return valueText;
	}

	private <T> TextField addTextField(GridPane gridPane, int rowIndex, String label, Property<T> property, Format format) {
    	gridPane.add(new Text(label), 0, rowIndex);
    	TextField valueTextField = new TextField();
		Bindings.bindBidirectional(valueTextField.textProperty(), property, format);
    	gridPane.add(valueTextField, 1, rowIndex);
    	return valueTextField;
	}

	private Slider addSlider(GridPane gridPane, int rowIndex, String label, DoubleProperty doubleProperty, double min, double max, double value) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        Slider valueSlider = new Slider(min, max, value);
        Bindings.bindBidirectional(doubleProperty, valueSlider.valueProperty());
		gridPane.add(valueSlider, 1, rowIndex);
		return valueSlider;
	}

	private CheckBox addCheckBox(GridPane gridPane, int rowIndex, String label, BooleanProperty booleanProperty) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        CheckBox valueCheckBox = new CheckBox();
        Bindings.bindBidirectional(booleanProperty, valueCheckBox.selectedProperty());
		gridPane.add(valueCheckBox, 1, rowIndex);
		return valueCheckBox;
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

	private void createRandomPlanet() {
		seedProperty.set(Math.abs(new java.util.Random().nextInt()));

    	updateRandomPlanet(true);
	}
	
	private void updateRandomPlanet(boolean overwriteProperties) {
		PlanetData planetData = planetGenerator.createPlanetData(seedProperty.get());

		plantGrowthBox.getChildren().clear();
		mapPlantDataToRectangle.clear();
		
		if (overwriteProperties) {
			radiusProperty.set(planetData.radius);
			minHeightProperty.set(planetData.minHeight);
			maxHeightProperty.set(planetData.maxHeight);
			hasOceanProperty.set(planetData.hasOcean);
			craterDensityProperty.set(planetData.craterDensity);
			baseTemperatureProperty.set(planetData.baseTemperature);
			seasonalBaseTemperatureVariationProperty.set(planetData.seasonalBaseTemperatureVariation);
			dailyBaseTemperatureVariationProperty.set(planetData.dailyBaseTemperatureVariation);
			plantsProperty.set(FXCollections.observableArrayList(planetData.plants));
			cratersProperty.set(FXCollections.observableArrayList(CraterLayer.simpleRoundCrater, CraterLayer.simpleFlatCrater, CraterLayer.complexFlatCrater, CraterLayer.complexStepsCrater));
			seasonTemperatureInfluenceToAverageProperty.set(planetData.seasonTemperatureInfluenceToAverage);
			dailyTemperatureInfluenceToAverageProperty.set(planetData.dailyTemperatureInfluenceToAverage);
			dailyTemperatureGroundDelayProperty.set(planetData.dailyTemperatureGroundDelay);
			dailyTemperatureOceanDelayProperty.set(planetData.dailyTemperatureOceanDelay);
			dailyTemperatureOceanFactorProperty.set(planetData.dailyTemperatureOceanFactor);
			seasonProperty.set(planetData.season);
			dayTimeProperty.set(planetData.dayTime);
		} else {
			planetData.radius = radiusProperty.get();
			planetData.minHeight = minHeightProperty.get();
			planetData.maxHeight = maxHeightProperty.get();
			planetData.hasOcean = hasOceanProperty.get();
			planetData.craterDensity = craterDensityProperty.get();
			planetData.baseTemperature = baseTemperatureProperty.get();
			planetData.seasonalBaseTemperatureVariation = seasonalBaseTemperatureVariationProperty.get();
			planetData.dailyBaseTemperatureVariation = dailyBaseTemperatureVariationProperty.get();
			planetData.seasonTemperatureInfluenceToAverage = seasonTemperatureInfluenceToAverageProperty.get();
			planetData.dailyTemperatureInfluenceToAverage = dailyTemperatureInfluenceToAverageProperty.get();
			planetData.dailyTemperatureGroundDelay = dailyTemperatureGroundDelayProperty.get();
			planetData.dailyTemperatureOceanDelay = dailyTemperatureOceanDelayProperty.get();
			planetData.dailyTemperatureOceanFactor = dailyTemperatureOceanFactorProperty.get();
			planetData.season = seasonProperty.get();
			planetData.dayTime = dayTimeProperty.get();
		}
		
		long startNanoTime = System.nanoTime();
    	planet = generatePlanet(planetData, new Random(planetData.seed));
    	long endNanoTime = System.nanoTime();
    	renderMillisecondsProperty.set((endNanoTime - startNanoTime) / 1000000.0);

		if (overwriteProperties) {
	    	updateZoomImages(0, 180);
		} else {
			updateZoomImages(zoomLatitudeDegrees, zoomLongitudeDegrees);
		}
	}
	
	private Planet generatePlanet(PlanetData planetData, Random random) {
		Planet planet = planetGenerator.createPlanet(planetData, random);
		
		PlanetGenerationContext context = planet.createDefaultContext();
		context.textureTypes.add(TextureType.DIFFUSE);
		context.textureTypes.add(TextureType.NORMAL);
		context.textureTypes.add(TextureType.LUMINOUS);
		context.textureTypes.add(TextureType.PRECIPITATION);
		context.textureTypes.add(TextureType.PRECIPITATION_AVERAGE);
		context.textureTypes.add(TextureType.THERMAL);
		context.textureTypes.add(TextureType.THERMAL_AVERAGE);
		JavafxPlanetTextures planetTextures = new JavafxPlanetTextures(TEXTURE_IMAGE_WIDTH, TEXTURE_IMAGE_HEIGHT, context);
		planet.getTextures(TEXTURE_IMAGE_WIDTH, TEXTURE_IMAGE_HEIGHT, context, planetTextures);
		
		Image diffuseImage = planetTextures.getImage(TextureType.DIFFUSE);
		Image normalImage = planetTextures.getImage(TextureType.NORMAL);
		Image luminousImage = planetTextures.getImage(TextureType.LUMINOUS);
		Image thermalImage = planetTextures.getImage(TextureType.THERMAL);
		Image thermalAverageImage = planetTextures.getImage(TextureType.THERMAL_AVERAGE);
		Image precipitationImage = planetTextures.getImage(TextureType.PRECIPITATION);
		Image precipitationAverageImage = planetTextures.getImage(TextureType.PRECIPITATION_AVERAGE);
		
		diffuseImageView.setImage(diffuseImage);
		normalImageView.setImage(normalImage);
		luminousImageView.setImage(luminousImage);
		thermalImageView.setImage(thermalImage);
		thermalAverageImageView.setImage(thermalAverageImage);
		precipitationImageView.setImage(precipitationImage);
		precipitationAverageImageView.setImage(precipitationAverageImage);
		
		material.setDiffuseMap(diffuseImage);
		material.setBumpMap(normalImage);
		material.setSelfIlluminationMap(luminousImage); // TODO show only in dark side - but javafx cannot do that
		
		return planet;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
