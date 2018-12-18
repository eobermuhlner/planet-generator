package ch.obermuhlner.planetgen.javafx.viewer;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.planet.ColorScale;
import ch.obermuhlner.planetgen.planet.DoubleMap;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.texture.TextureType;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.BasicCraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.planetgen.planet.texture.TextureWriter;
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
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetGeneratorJavafxApp extends Application {

	private static final boolean SHOW_DEBUG_VALUE = true;
	
	private int ZOOM_IMAGE_SIZE = 128;
	private int ZOOM_HIRES_IMAGE_SIZE = 1024;
	private int ZOOM_TERRAIN_SIZE = 128;

	private int TEXTURE_IMAGE_WIDTH = 1024;
	private int TEXTURE_IMAGE_HEIGHT = TEXTURE_IMAGE_WIDTH / 2;

	private int HEIGHTMAP_HEIGHT = 256;

	private int MAP_WIDTH = 1024;
	
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
	
	private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");

	private ImageView diffuseImageView;
	private ImageView specularImageView;
	private ImageView normalImageView;
	private ImageView luminousImageView;
	private ImageView heightImageView;
	private ImageView thermalImageView;
	private ImageView thermalAverageImageView;
	private ImageView precipitationImageView;
	private ImageView precipitationAverageImageView;
	private ImageView pressureImageView;
	private ImageView cloudImageView;
	private ImageView debugImageView;

	private PhongMaterial planetMaterial;
	private PhongMaterial cloudMaterial;
	private PhongMaterial terrainMaterial;
	private TriangleMesh terrainMesh;
	
	private LongProperty seedProperty = new SimpleLongProperty();
	private LongProperty timeProperty = new SimpleLongProperty();
	private LongProperty revolutionTimeProperty = new SimpleLongProperty();
	private LongProperty orbitTimeProperty = new SimpleLongProperty();
	private DoubleProperty radiusProperty = new SimpleDoubleProperty();
	private DoubleProperty minHeightProperty = new SimpleDoubleProperty();
	private DoubleProperty maxHeightProperty = new SimpleDoubleProperty();
	private BooleanProperty hasOceanProperty = new SimpleBooleanProperty();
	private DoubleProperty craterDensityProperty = new SimpleDoubleProperty();
	private DoubleProperty volcanoDensityProperty = new SimpleDoubleProperty();
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
	
	private DoubleProperty latitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty longitudeProperty = new SimpleDoubleProperty(0);
	private DoubleProperty heightProperty = new SimpleDoubleProperty(0);
	private DoubleProperty reefHeightProperty = new SimpleDoubleProperty(0);
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
	private ImageView zoomThermalImageView;
	private ImageView zoomPrecipitationAverageImageView;
	private double zoomLongitudeDegrees;
	private double zoomLatitudeDegrees;
	private double zoomLatitudeSize;
	private double zoomLongitudeSize;

	private Canvas zoomHeightMapCanvas;

	private Canvas heightMapCanvas;

	private VBox plantGrowthBox;
	private final Map<String, Rectangle> mapPlantDataToRectangle = new HashMap<>();

	private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		boolean smallMode = Screen.getPrimary().getBounds().getHeight() <= 800;

		if (smallMode) {
			ZOOM_IMAGE_SIZE /= 2;
			ZOOM_HIRES_IMAGE_SIZE /= 2;
			ZOOM_TERRAIN_SIZE /= 2;
			TEXTURE_IMAGE_WIDTH /= 2;
			TEXTURE_IMAGE_HEIGHT /= 2;
			HEIGHTMAP_HEIGHT /= 2;
			MAP_WIDTH /= 2;
		}

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
        	addText(infoGridPane, rowIndex++, "Reef Height [m]", reefHeightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Ice Height [m]", iceHeightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Snow Height [m]", snowHeightProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Temperature [K]", temperatureProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Precipitation Average", precipitationAverageProperty, DOUBLE_FORMAT);
        	addText(infoGridPane, rowIndex++, "Render Time [ms]", renderMillisecondsProperty, DOUBLE_FORMAT);
        	if (SHOW_DEBUG_VALUE) {
        		addText(infoGridPane, rowIndex++, "Debug", debugProperty, DOUBLE_FORMAT);
        	}

        	addSlider(infoGridPane, rowIndex++, "Zoom", zoomProperty, 20, 10000, 50);
        	zoomProperty.addListener((source, oldValue, newValue) -> updateZoomImages(zoomLatitudeDegrees, zoomLongitudeDegrees, false));
        	addTextField(infoGridPane, rowIndex++, "", zoomProperty, INTEGER_FORMAT);

			zoomDiffuseImageView = new ImageView();
			infoGridPane.add(zoomDiffuseImageView, 0, rowIndex, 1, 1);
			setDragZoomMapEvents(zoomDiffuseImageView);

        	zoomNormalImageView = new ImageView();
        	infoGridPane.add(zoomNormalImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomNormalImageView);

			zoomThermalImageView = new ImageView();
			infoGridPane.add(zoomThermalImageView, 0, rowIndex, 1, 1);
			setDragZoomMapEvents(zoomThermalImageView);

			zoomPrecipitationAverageImageView = new ImageView();
        	infoGridPane.add(zoomPrecipitationAverageImageView, 1, rowIndex++, 1, 1);
        	setDragZoomMapEvents(zoomPrecipitationAverageImageView);

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
        TabPane mainTabPane = new TabPane();
        mapBox.getChildren().add(mainTabPane);
        mainTabPane.setMaxWidth(MAP_WIDTH);
        mainTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        heightMapCanvas = new Canvas(MAP_WIDTH, HEIGHTMAP_HEIGHT);
        mapBox.getChildren().add(heightMapCanvas);

		TabPane renderingTabPane = addSubTab(mainTabPane, "Rendering");

		diffuseImageView = addTabImageView(renderingTabPane, "Color");
		specularImageView = addTabImageView(renderingTabPane, "Specular");
		normalImageView = addTabImageView(renderingTabPane, "Normal");
		luminousImageView = addTabImageView(renderingTabPane, "Luminous");

		TabPane atmosphereTabPane = addSubTab(mainTabPane, "Atmosphere");

		cloudImageView = addTabImageView(atmosphereTabPane, Color.GRAY, "Cloud");

		TabPane infoTabPane = addSubTab(mainTabPane, "Info");

        heightImageView = addTabImageView(infoTabPane, "Height");
        thermalImageView = addTabImageView(infoTabPane, "Thermal");
        thermalAverageImageView = addTabImageView(infoTabPane, "Thermal Average");
        precipitationImageView = addTabImageView(infoTabPane, "Precipitation");
        precipitationAverageImageView = addTabImageView(infoTabPane, "Precipitation Average");
        pressureImageView = addTabImageView(infoTabPane, "Atmospheric Pressure");

        // 2D debug texture
		if (SHOW_DEBUG_VALUE) {
			TabPane debugTabPane = addSubTab(mainTabPane, "Debug");

			debugImageView = addTabImageView(debugTabPane, "Debug");
		}

		TabPane modelsTabPane = addSubTab(mainTabPane, "Models");

		// info plants
		modelsTabPane.getTabs().add(new Tab("Plants", createPlantInfoView()));

        // info craters
		modelsTabPane.getTabs().add(new Tab("Craters", createCratersInfoView()));

		TabPane threedTabPane = addSubTab(mainTabPane, "3D");

		// 3D planet
    	StackPane node3dPlanetContainer = new StackPane();
		threedTabPane.getTabs().add(new Tab("3D Planet", node3dPlanetContainer));
    	planetMaterial = new PhongMaterial(Color.WHITE);
    	cloudMaterial = new PhongMaterial(Color.WHITE);
    	node3dPlanetContainer.getChildren().add(createPlanetNode3D(node3dPlanetContainer, planetMaterial, cloudMaterial));
    	
        // 3D zoom terrain
    	StackPane node3dTerrainContainer = new StackPane();
		threedTabPane.getTabs().add(new Tab("3D Zoom", node3dTerrainContainer));
    	terrainMaterial = new PhongMaterial(Color.WHITE);
    	terrainMesh = new TriangleMesh();
		node3dTerrainContainer.getChildren().add(createTerrainNode3D(node3dTerrainContainer, terrainMaterial, terrainMesh));
    	
        // editor grid pane
        GridPane editorGridPane = new GridPane();
        viewBorderPane.setLeft(editorGridPane);
        editorGridPane.setHgap(4);
        editorGridPane.setVgap(4);
        BorderPane.setMargin(editorGridPane, new Insets(4));
        {
        	int rowIndex = 0;

        	HBox createButtonBox = new HBox();
			editorGridPane.add(createButtonBox, 0, rowIndex++, 2, 1);

        	Button createRandomPlanetButton = new Button("Create Random Planet");
        	createButtonBox.getChildren().add(createRandomPlanetButton);
	        createRandomPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            createRandomPlanet();
	        });

			Button createPreviousPlanetButton = new Button("<");
			createButtonBox.getChildren().add(createPreviousPlanetButton);
			createPreviousPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
				createPreviousPlanet();
			});

			Button createNextPlanetButton = new Button(">");
			createButtonBox.getChildren().add(createNextPlanetButton);
			createNextPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
				createNextPlanet();
			});

			addTextField(editorGridPane, rowIndex++, "Seed", seedProperty, INTEGER_FORMAT);
        	addTextField(editorGridPane, rowIndex++, "Time", timeProperty, INTEGER_FORMAT);
        	addTimeButtons(editorGridPane, rowIndex++, timeProperty);

        	Button createPlanetButton = new Button("Create Planet");
	        editorGridPane.add(createPlanetButton, 0, rowIndex++, 2, 1);
	        createPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            updateRandomPlanet(true);
	        });

        	addTextField(editorGridPane, rowIndex++, "Revolution Time [ms]", revolutionTimeProperty, INTEGER_FORMAT);
        	addTextField(editorGridPane, rowIndex++, "Orbit Time [ms]", orbitTimeProperty, INTEGER_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Radius [m]", radiusProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Min Height [m]", minHeightProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Max Height [m]", maxHeightProperty, DOUBLE_FORMAT);
	        addCheckBox(editorGridPane, rowIndex++, "Ocean", hasOceanProperty);
	        addSlider(editorGridPane, rowIndex++, "Crater Density", craterDensityProperty, 0.0, 1.0, 0.0);
	        addSlider(editorGridPane, rowIndex++, "Volcano Density", volcanoDensityProperty, 0.0, 1.0, 0.0);
	        addTextField(editorGridPane, rowIndex++, "Base Temperature [K]", baseTemperatureProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Seasonal Variation [K]", seasonalBaseTemperatureVariationProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Variation [K]", dailyBaseTemperatureVariationProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Season Temperature Influence", seasonTemperatureInfluenceToAverageProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Influence", dailyTemperatureInfluenceToAverageProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ground Delay", dailyTemperatureGroundDelayProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ocean Delay", dailyTemperatureOceanDelayProperty, DOUBLE_FORMAT);
	        addTextField(editorGridPane, rowIndex++, "Daily Temperature Ocean Factor ", dailyTemperatureOceanFactorProperty, DOUBLE_FORMAT);
	        
        	Button updatePlanetButton = new Button("Update Planet");
	        editorGridPane.add(updatePlanetButton, 0, rowIndex++, 2, 1);
	        updatePlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
	            updateRandomPlanet(false);
	        });
	        
	        Button flightSimulatorButton = new Button("Flight Simulator");
	        editorGridPane.add(flightSimulatorButton, 0, rowIndex++, 2, 1);
	        flightSimulatorButton.addEventHandler(ActionEvent.ACTION, event -> {
	            startFlightSimulator();
	        });
        }
        
        // initial run
        createRandomPlanet();
        
    	primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(event -> {
        	threadExecutor.shutdown();
        });
	}

	private Node createPlantInfoView() {
        BorderPane borderPane = new BorderPane();

        ListView<PlantData> plantsListView = new ListView<PlantData>();
        borderPane.setLeft(plantsListView);
		plantsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

        BorderPane centerBorderPane = new BorderPane();
        borderPane.setCenter(centerBorderPane);

        GridPane plantGridPane = new GridPane();
        centerBorderPane.setBottom(plantGridPane);
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

        StackPane canvasBox = new StackPane();
        centerBorderPane.setCenter(canvasBox);

		Canvas plantCanvas = new Canvas(ZOOM_IMAGE_SIZE, ZOOM_IMAGE_SIZE);
        canvasBox.getChildren().add(plantCanvas);
        plantCanvas.heightProperty().bind(canvasBox.heightProperty());
        plantCanvas.widthProperty().bind(canvasBox.widthProperty());

        plantsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldPlantData, newPlantData) -> {
				ObservableList<PlantData> selectedItems = plantsListView.getSelectionModel().getSelectedItems();
                drawPlantGrowth(plantCanvas, selectedItems);

                if (newPlantData != null) {
					plantNameProperty.set(newPlantData.name);
					plantColorRectangle.setFill(ColorUtil.toJavafxColor(newPlantData.color));
					plantTemperatureOptimumProperty.set(newPlantData.temperatureOptimum);
					plantTemperatureMinimumProperty.set(newPlantData.temperatureOptimum - newPlantData.temperatureMinusDeviation);
					plantTemperatureMaximumProperty.set(newPlantData.temperatureOptimum + newPlantData.temperaturePlusDeviation);
					plantPrecipitationOptimumProperty.set(newPlantData.precipitationOptimum);
					plantPrecipitationMinimumProperty.set(newPlantData.precipitationOptimum - newPlantData.precipitationMinusDeviation);
					plantPrecipitationMaximumProperty.set(newPlantData.precipitationOptimum + newPlantData.precipitationPlusDeviation);
				}
            });
        
        return borderPane;
	}
	
	private Node createCratersInfoView() {
		ScrollPane scrollPane = new ScrollPane();
		HBox hBox = new HBox();
		scrollPane.setContent(hBox);

		ListView<Crater> cratersListView = new ListView<Crater>();
        hBox.getChildren().add(cratersListView);
        cratersListView.itemsProperty().bind(cratersProperty);
        cratersProperty.addListener(new ListChangeListener<Crater>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Crater> c) {
				cratersListView.getSelectionModel().selectFirst();
			}
		});

		LineChart<Number, Number> lineChart = new LineChart<>(new NumberAxis(), new NumberAxis());
		lineChart.setCreateSymbols(false);
        hBox.getChildren().add(lineChart);
		
		ObservableList<Series<Number, Number>> data = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> heightData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Height", heightData));
		ObservableList<Data<Number, Number>> heightNoiseData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Vertical Height Noise", heightNoiseData));
		ObservableList<Data<Number, Number>> radialNoiseData = FXCollections.observableArrayList();
		data.add(new XYChart.Series<>("Radial Noise", radialNoiseData));

		Canvas craterCanvas = new Canvas(200, 200);
		hBox.getChildren().add(craterCanvas);

        cratersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldCrater, newCrater) -> {
        	if (newCrater == null) {
        		return;
        	}
        	heightData.clear();
        	heightNoiseData.clear();
        	radialNoiseData.clear();
        	double minHeight = Double.MAX_VALUE;
        	double maxHeight = Double.MIN_VALUE;
    		for (double x = -1.1; x <= 1.1; x+=0.005) {
    			double height = newCrater.heightFunction.calculate(x);
    			minHeight = Math.min(minHeight, height);
    			maxHeight = Math.max(maxHeight, height);
				heightData.add(new XYChart.Data<>(x, height));
    			heightNoiseData.add(new XYChart.Data<>(x, newCrater.verticalHeightNoiseFunction.calculate(x)));
    			radialNoiseData.add(new XYChart.Data<>(x, newCrater.radialNoiseFunction.calculate(x)));
    		}

    		BasicCraterCalculator craterCalculator = new BasicCraterCalculator(newCrater); 
    		PlanetGenerationContext context = new PlanetGenerationContext();
    		context.accuracy = 0.00001;
    		ColorScale colorScale = ColorScale.divergingScale(minHeight, 0, maxHeight);

    		GraphicsContext gc = craterCanvas.getGraphicsContext2D();
    		for (int x = 0; x < craterCanvas.getWidth(); x++) {
        		for (int y = 0; y < craterCanvas.getHeight(); y++) {
					Vector2 point = Vector2.of(
							x / craterCanvas.getWidth(),
							y / craterCanvas.getHeight());
					double height = craterCalculator.calculateCrater(point, context);
		    		Color color = ColorUtil.toJavafxColor(colorScale.toColor(height));
		    		gc.setFill(color);
		    		gc.fillRect(x, y, 1, 1);
    			}
			}
        });
		lineChart.dataProperty().set(data);
		
		return scrollPane;
	}

	private void drawPlantGrowth(Canvas canvas, ObservableList<PlantData> plantDatas) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		for (int y = 0; y < canvas.getHeight(); y++) {
			for (int x = 0; x < canvas.getWidth(); x++) {
				Color color = Color.BEIGE;
				for (PlantData plantData : plantDatas) {
					double temperature = y / canvas.getHeight() * 50.0 + 260.0;
					double precipitation = x / canvas.getWidth() * 2.0;
					double plant = plantData.plantGrowth(temperature, precipitation);
                    Color plantColor = ColorUtil.toJavafxColor(plantData.color);
					color = color.interpolate(plantColor, plant);
				}
				gc.setFill(color);
				gc.fillRect(x, y, 1, 1);
			}
		}
	}

	private TabPane addSubTab(TabPane tabPane, String name) {
		TabPane subTabPane = new TabPane();
		subTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getTabs().add(new Tab(name, subTabPane));

		return subTabPane;
	}

	private ImageView addTabImageView(TabPane tabPane, String name) {
		ImageView imageView = new ImageView();
        imageView.setFitWidth(MAP_WIDTH);
        imageView.setPreserveRatio(true);
        
       	tabPane.getTabs().add(new Tab(name, imageView));
        setInfoAndZoomEvents(imageView);
        
        return imageView;
	}
		
	private ImageView addTabImageView(TabPane tabPane, Color backgroundColor, String name) {
		ImageView imageView = new ImageView();
        imageView.setFitWidth(MAP_WIDTH);
        imageView.setPreserveRatio(true);
        
    	StackPane stackPane = new StackPane();
    	stackPane.setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));
    	stackPane.getChildren().add(imageView);
    	tabPane.getTabs().add(new Tab(name, stackPane));
        
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
			double latitudeDegrees = toLatitude(-deltaY, imageView.getImage(), zoomLatitudeSize * imageView.getImage().getHeight());

			updateZoomImages(zoomLatitudeDegrees + latitudeDegrees, zoomLongitudeDegrees + longitudeDegrees, false);
		});
		imageView.setOnMouseReleased(event -> {
			double deltaX = event.getX() - lastMouseDragX;
			double deltaY = event.getY() - lastMouseDragY;
			lastMouseDragX = event.getX();
			lastMouseDragY = event.getY();
			
			double longitudeDegrees = toLongitude(-deltaX, imageView.getImage(), zoomLongitudeSize * imageView.getImage().getWidth());
			double latitudeDegrees = toLatitude(-deltaY, imageView.getImage(), zoomLatitudeSize * imageView.getImage().getHeight());

			updateZoomImages(zoomLatitudeDegrees + latitudeDegrees, zoomLongitudeDegrees + longitudeDegrees, true);
		});
	}

	private void setInfoAndZoomEvents(ImageView imageView) {
		imageView.setOnMouseClicked(event -> {
        	imageInfoAndZoomMouseEvent(event, imageView, true);
        });
		imageView.setOnMouseDragged(event -> {
        	imageInfoAndZoomMouseEvent(event, imageView, false);
        });
		imageView.setOnMouseReleased(event -> {
        	imageInfoAndZoomMouseEvent(event, imageView, true);
        });
	}

	private void imageInfoAndZoomMouseEvent(MouseEvent event, ImageView imageView, boolean hires) {
		double longitudeDegrees = toLongitude(event.getX(), imageView.getImage(), 360);
		double latitudeDegrees = toLatitude(event.getY(), imageView.getImage(), 180);
		
		updateZoomImages(latitudeDegrees, longitudeDegrees, hires);
	}
	
	private void updateZoomImages(double latitudeDegrees, double longitudeDegrees, boolean hires) {
		longitudeDegrees = MathUtil.clamp(longitudeDegrees, 0, 360);
		latitudeDegrees = MathUtil.clamp(latitudeDegrees, 0, 180);

		zoomLongitudeDegrees = longitudeDegrees;
		zoomLatitudeDegrees = latitudeDegrees;
		
		longitudeProperty.set(longitudeDegrees);
		latitudeProperty.set(latitudeDegrees);
		
		PlanetGenerationContext context = planet.createDefaultContext();
		context.accuracy = 0.1 / zoomProperty.get();
		context.textureTypes.add(TextureType.DIFFUSE);
		context.textureTypes.add(TextureType.SPECULAR);
		context.textureTypes.add(TextureType.NORMAL);
		context.textureTypes.add(TextureType.THERMAL);
		context.textureTypes.add(TextureType.PRECIPITATION_AVERAGE);

		double latitudeRadians =  Math.toRadians(latitudeDegrees);
		double longitudeRadians = Math.toRadians(longitudeDegrees);
		PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
		heightProperty.set(planetPoint.groundHeight);
		reefHeightProperty.set(planetPoint.reefHeight);
		iceHeightProperty.set(planetPoint.iceHeight);
		snowHeightProperty.set(planetPoint.snowHeight);
		temperatureProperty.set(planetPoint.temperature);
		precipitationAverageProperty.set(planetPoint.precipitationAverage);
		debugProperty.set(planetPoint.debug);
		
		zoomLatitudeSize = Planet.RANGE_LATITUDE / zoomProperty.get() * 2;
		zoomLongitudeSize = Planet.RANGE_LONGITUDE / zoomProperty.get();
		DoubleMap terrainHeightMap = new DoubleMap(ZOOM_TERRAIN_SIZE, ZOOM_TERRAIN_SIZE);
		Map<TextureType, TextureWriter<Image>> textures = planet.getTextures(
				latitudeRadians - zoomLatitudeSize,
				latitudeRadians + zoomLatitudeSize,
				longitudeRadians - zoomLongitudeSize,
				longitudeRadians + zoomLongitudeSize,
				ZOOM_IMAGE_SIZE, ZOOM_IMAGE_SIZE,
				context, (width, height, textureType) -> new JavafxTextureWriter(width, height),
				terrainHeightMap
		);

		zoomDiffuseImageView.setImage(textures.get(TextureType.DIFFUSE).getTexture());
		zoomNormalImageView.setImage(textures.get(TextureType.NORMAL).getTexture());
		zoomThermalImageView.setImage(textures.get(TextureType.THERMAL).getTexture());
		zoomPrecipitationAverageImageView.setImage(textures.get(TextureType.PRECIPITATION_AVERAGE).getTexture());

		if (hires) {
			threadExecutor.submit(() -> {
				PlanetGenerationContext hiresContext = planet.createDefaultContext();
				hiresContext.accuracy = 0.1 / zoomProperty.get();
				hiresContext.textureTypes.add(TextureType.DIFFUSE);
				hiresContext.textureTypes.add(TextureType.SPECULAR);
				hiresContext.textureTypes.add(TextureType.NORMAL);

				Map<TextureType, TextureWriter<Image>> hiresTextures = planet.getTextures(
						latitudeRadians - zoomLatitudeSize,
						latitudeRadians + zoomLatitudeSize,
						longitudeRadians - zoomLongitudeSize,
						longitudeRadians + zoomLongitudeSize,
						ZOOM_HIRES_IMAGE_SIZE,
						ZOOM_HIRES_IMAGE_SIZE,
						hiresContext, (width, height, textureType) -> new JavafxTextureWriter(width, height),
						null
				);
				terrainMaterial.setDiffuseMap(hiresTextures.get(TextureType.DIFFUSE).getTexture());
				terrainMaterial.setSpecularMap(hiresTextures.get(TextureType.SPECULAR).getTexture());
				terrainMaterial.setBumpMap(hiresTextures.get(TextureType.NORMAL).getTexture());
			});
		} else {
			terrainMaterial.setDiffuseMap(textures.get(TextureType.DIFFUSE).getTexture());
			terrainMaterial.setSpecularMap(textures.get(TextureType.SPECULAR).getTexture());
			terrainMaterial.setBumpMap(textures.get(TextureType.NORMAL).getTexture());
		}
		
		updateTerrain(terrainHeightMap);
		
		drawHeightMap(heightMapCanvas, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, latitudeRadians);
		drawHeightMap(zoomHeightMapCanvas, longitudeRadians - zoomLongitudeSize, longitudeRadians + zoomLongitudeSize, latitudeRadians);
		
		if (planetPoint.plants != null) {
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
		} else {
			for (Rectangle plantGrowthBar : mapPlantDataToRectangle.values()) {
				plantGrowthBar.setWidth(0);
			}
		}
	}

	private void updateTerrain(DoubleMap terrainHeightMap) {
		ObservableFloatArray points = terrainMesh.getPoints();
		double planetHeightRange = planet.planetData.maxHeight - planet.planetData.minHeight;
		int pointIndex = 0;
		for (int y = 0; y <= terrainHeightMap.height; y++) {
			double height = 0;
			for (int x = 0; x < terrainHeightMap.width; x++) {
				int yy = y == terrainHeightMap.height ? y-1 : y; // workaround n+1 texture size - special handling last row
				height = - terrainHeightMap.getValue(x, yy) / planetHeightRange * 0.1;

				points.set(pointIndex+1, (float) height);
				pointIndex+=3;
			}
			
			// workaround n+1 texture size - set the same height again in the last points
			points.set(pointIndex+1, (float) height);
			pointIndex+=3;
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
			
			if (point.cloud > 0) {
				double cloudMinHeight = (point.cloudMinHeight - planet.planetData.minHeight) * heightFactor;
				double cloudMaxHeight = (point.cloudMaxHeight - planet.planetData.minHeight) * heightFactor;
				gc.setStroke(ColorUtil.toJavafxColor(Planet.toCloudColor(point.cloud)));
				gc.strokeLine(x, canvasHeight - cloudMinHeight, x, canvasHeight - cloudMaxHeight);
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
	
	private void addTimeButtons(GridPane gridPane, int rowIndex, LongProperty timeProperty) {
		HBox box = new HBox();

		box.getChildren().add(createTimeDeltaButton(timeProperty, "-m", -PlanetGenerator.DAYS * 30));
		box.getChildren().add(createTimeDeltaButton(timeProperty, "-d", -PlanetGenerator.DAYS));
		box.getChildren().add(createTimeDeltaButton(timeProperty, "-h", -PlanetGenerator.HOURS));
		box.getChildren().add(createTimeDeltaButton(timeProperty, "+h", +PlanetGenerator.HOURS));
		box.getChildren().add(createTimeDeltaButton(timeProperty, "+d", +PlanetGenerator.DAYS));
		box.getChildren().add(createTimeDeltaButton(timeProperty, "+m", +PlanetGenerator.DAYS * 30));
		
		gridPane.add(box, 1, rowIndex);
	}
	
	private Button createTimeDeltaButton(LongProperty property, String name, long deltaMillis) {
		Button button = new Button(name);
		button.addEventHandler(ActionEvent.ACTION, event -> {
			property.set(property.get() + deltaMillis);
		});
		return button;
	}

	private Node createPlanetNode3D(Region container, PhongMaterial planetMaterial, PhongMaterial cloudMaterial) {
		Group world = new Group(); 
		
		Sphere planetSphere = new Sphere();
		planetSphere.setMaterial(planetMaterial);
		planetSphere.setRotationAxis(Rotate.Y_AXIS);
        world.getChildren().add(planetSphere);
        
		Sphere cloudSphere = new Sphere(1.02);
		cloudSphere.setMaterial(cloudMaterial);
		cloudSphere.setRotationAxis(Rotate.Y_AXIS);
        world.getChildren().add(cloudSphere);

		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateX(10.0);
		world.getChildren().add(light);

		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				planetSphere.setRotate(planetSphere.getRotate() + 0.01);
				cloudSphere.setRotate(cloudSphere.getRotate() + 0.01);
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
	
	private Node createTerrainNode3D(Region container, PhongMaterial material, TriangleMesh mesh) {
		Group world = new Group(); 

		float[] texCoords = new float[2 * (ZOOM_TERRAIN_SIZE+1) * (ZOOM_TERRAIN_SIZE+1)];
		float[] points = new float[3 * (ZOOM_TERRAIN_SIZE+1) * (ZOOM_TERRAIN_SIZE+1)];
		int[] faces = new int[6 * 2 * ZOOM_TERRAIN_SIZE * ZOOM_TERRAIN_SIZE];
		
		float xStep = 1f / ZOOM_TERRAIN_SIZE;
		float yStep = 1f / ZOOM_TERRAIN_SIZE;
		int texCoordIndex = 0;
		int pointIndex = 0;
		int faceIndex = 0;
		int facePointIndex = 0;
		for (int y = 0; y <= ZOOM_TERRAIN_SIZE; y++) {
			for (int x = 0; x <= ZOOM_TERRAIN_SIZE; x++) {
				float meshX = x * xStep;
				float meshY = y * yStep;
				
				texCoords[texCoordIndex++] = meshX;
				texCoords[texCoordIndex++] = meshY;
				
				points[pointIndex++] = meshY;
				points[pointIndex++] = 0;
				points[pointIndex++] = meshX;
				
				if (x != ZOOM_TERRAIN_SIZE && y != ZOOM_TERRAIN_SIZE) {
					faces[faceIndex++] = facePointIndex+0;
					faces[faceIndex++] = facePointIndex+0;
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+1;
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+1;
					faces[faceIndex++] = facePointIndex+1;
					faces[faceIndex++] = facePointIndex+1;
					
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+2;
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+2;
					faces[faceIndex++] = facePointIndex+1;
					faces[faceIndex++] = facePointIndex+1;
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+1;
					faces[faceIndex++] = facePointIndex+ZOOM_TERRAIN_SIZE+1;
				}
				
				facePointIndex++;
			}
		}

		terrainMesh.getTexCoords().setAll(texCoords);
		terrainMesh.getPoints().setAll(points);
		terrainMesh.getFaces().setAll(faces);

		MeshView meshView = new MeshView(mesh);
		meshView.setMaterial(material);
		meshView.setTranslateX(-0.5);
		
        world.getChildren().add(meshView);
        meshView.setRotationAxis(Rotate.Y_AXIS);
        
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				meshView.setRotate(meshView.getRotate() + 0.01);
			}
		}));
		timeline.playFromStart();

		PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(2.0);
        light.setTranslateY(-10.0);
		light.setTranslateZ(20.0);
		world.getChildren().add(light);

		PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
        		new Rotate(5, Rotate.Y_AXIS),
        		new Rotate(-10, Rotate.X_AXIS),
        		new Translate(0, -0.15, -0.32)
        		);
        camera.setNearClip(0.0001);
        camera.setFarClip(2.0);
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
		
		//timeProperty.set(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
		timeProperty.set(0L);

    	updateRandomPlanet(true);
	}

	private void createPreviousPlanet() {
		seedProperty.set(seedProperty.get() - 1);
		timeProperty.set(0L);

		updateRandomPlanet(true);
	}

	private void createNextPlanet() {
		seedProperty.set(seedProperty.get() + 1);
		timeProperty.set(0L);

		updateRandomPlanet(true);
	}

	private void updateRandomPlanet(boolean overwriteProperties) {
		PlanetData planetData = planetGenerator.createPlanetData(seedProperty.get());

		plantGrowthBox.getChildren().clear();
		mapPlantDataToRectangle.clear();
		
		if (overwriteProperties) {
			revolutionTimeProperty.set(planetData.revolutionTime);
			orbitTimeProperty.set(planetData.orbitTime);
			radiusProperty.set(planetData.radius);
			minHeightProperty.set(planetData.minHeight);
			maxHeightProperty.set(planetData.maxHeight);
			hasOceanProperty.set(planetData.hasOcean);
			craterDensityProperty.set(planetData.craterDensity);
			volcanoDensityProperty.set(planetData.volcanoDensity);
			baseTemperatureProperty.set(planetData.baseTemperature);
			seasonalBaseTemperatureVariationProperty.set(planetData.seasonalBaseTemperatureVariation);
			dailyBaseTemperatureVariationProperty.set(planetData.dailyBaseTemperatureVariation);
			plantsProperty.set(FXCollections.observableArrayList(planetData.plants));
			cratersProperty.set(FXCollections.observableArrayList(planetData.craters));
			seasonTemperatureInfluenceToAverageProperty.set(planetData.seasonTemperatureInfluenceToAverage);
			dailyTemperatureInfluenceToAverageProperty.set(planetData.dailyTemperatureInfluenceToAverage);
			dailyTemperatureGroundDelayProperty.set(planetData.dailyTemperatureGroundDelay);
			dailyTemperatureOceanDelayProperty.set(planetData.dailyTemperatureOceanDelay);
			dailyTemperatureOceanFactorProperty.set(planetData.dailyTemperatureOceanFactor);
		} else {
			planetData.time = timeProperty.get();
			planetData.revolutionTime = revolutionTimeProperty.get();
			planetData.orbitTime = orbitTimeProperty.get();
			planetData.radius = radiusProperty.get();
			planetData.minHeight = minHeightProperty.get();
			planetData.maxHeight = maxHeightProperty.get();
			planetData.hasOcean = hasOceanProperty.get();
			planetData.craterDensity = craterDensityProperty.get();
			planetData.volcanoDensity = volcanoDensityProperty.get();
			planetData.baseTemperature = baseTemperatureProperty.get();
			planetData.seasonalBaseTemperatureVariation = seasonalBaseTemperatureVariationProperty.get();
			planetData.dailyBaseTemperatureVariation = dailyBaseTemperatureVariationProperty.get();
			planetData.seasonTemperatureInfluenceToAverage = seasonTemperatureInfluenceToAverageProperty.get();
			planetData.dailyTemperatureInfluenceToAverage = dailyTemperatureInfluenceToAverageProperty.get();
			planetData.dailyTemperatureGroundDelay = dailyTemperatureGroundDelayProperty.get();
			planetData.dailyTemperatureOceanDelay = dailyTemperatureOceanDelayProperty.get();
			planetData.dailyTemperatureOceanFactor = dailyTemperatureOceanFactorProperty.get();
		}
		
		long startNanoTime = System.nanoTime();
    	planet = generatePlanet(planetData);
    	long endNanoTime = System.nanoTime();
    	renderMillisecondsProperty.set((endNanoTime - startNanoTime) / 1000000.0);

		if (overwriteProperties) {
	    	updateZoomImages(0, 180, true);
		} else {
			updateZoomImages(zoomLatitudeDegrees, zoomLongitudeDegrees, true);
		}
	}
	
	private void startFlightSimulator() {
		FlightSimulator flightSimulator = new FlightSimulator(planet);
		flightSimulator.start();
	}
	
	private Planet generatePlanet(PlanetData planetData) {
		Planet planet = planetGenerator.createPlanet(planetData);
		
		PlanetGenerationContext context = planet.createDefaultContext();
		context.textureTypes.add(TextureType.DIFFUSE);
		context.textureTypes.add(TextureType.SPECULAR);
		context.textureTypes.add(TextureType.NORMAL);
		context.textureTypes.add(TextureType.LUMINOUS);
		context.textureTypes.add(TextureType.HEIGHT);
		context.textureTypes.add(TextureType.THERMAL);
		context.textureTypes.add(TextureType.THERMAL_AVERAGE);
		context.textureTypes.add(TextureType.PRECIPITATION);
		context.textureTypes.add(TextureType.PRECIPITATION_AVERAGE);
		context.textureTypes.add(TextureType.ATMOSPHERIC_PRESSURE);
		context.textureTypes.add(TextureType.CLOUD);
		if (SHOW_DEBUG_VALUE) {
			context.textureTypes.add(TextureType.DEBUG);
		}
		Map<TextureType, TextureWriter<Image>> textures = planet.getTextures(TEXTURE_IMAGE_WIDTH, TEXTURE_IMAGE_HEIGHT, context, (width, height, textureType) -> new JavafxTextureWriter(width, height));

		Image diffuseImage = textures.get(TextureType.DIFFUSE).getTexture();
		Image specularImage = textures.get(TextureType.SPECULAR).getTexture();
		Image normalImage = textures.get(TextureType.NORMAL).getTexture();
		Image luminousImage = textures.get(TextureType.LUMINOUS).getTexture();
		Image heightImage = textures.get(TextureType.HEIGHT).getTexture();
		Image thermalImage = textures.get(TextureType.THERMAL).getTexture();
		Image thermalAverageImage = textures.get(TextureType.THERMAL_AVERAGE).getTexture();
		Image precipitationImage = textures.get(TextureType.PRECIPITATION).getTexture();
		Image precipitationAverageImage = textures.get(TextureType.PRECIPITATION_AVERAGE).getTexture();
		Image pressureImage = textures.get(TextureType.ATMOSPHERIC_PRESSURE).getTexture();
		Image cloudImage = textures.get(TextureType.CLOUD).getTexture();
		Image debugImage = null;
		if (SHOW_DEBUG_VALUE) {
			debugImage = textures.get(TextureType.DEBUG).getTexture();
		}

		setImage(diffuseImageView, diffuseImage);
		setImage(specularImageView, specularImage);
		setImage(normalImageView, normalImage);
		setImage(luminousImageView, luminousImage);
		setImage(heightImageView, heightImage);
		setImage(thermalImageView, thermalImage);
		setImage(thermalAverageImageView, thermalAverageImage);
		setImage(precipitationImageView, precipitationImage);
		setImage(precipitationAverageImageView, precipitationAverageImage);
		setImage(pressureImageView, pressureImage);
		setImage(cloudImageView, cloudImage);
		if (SHOW_DEBUG_VALUE) {
			setImage(debugImageView, debugImage);
		}
		planetMaterial.setDiffuseMap(diffuseImage);
		planetMaterial.setSpecularMap(specularImage);
		planetMaterial.setBumpMap(normalImage);
		planetMaterial.setSelfIlluminationMap(luminousImage); // TODO show only in dark side - but javafx cannot do that

		cloudMaterial.setDiffuseMap(cloudImage);
		cloudMaterial.setSpecularMap(cloudImage); // actually not really correct - needs specular cloud texture

		return planet;
	}

	private void setImage(ImageView imageView, Image image) {
		if (imageView != null) {
			imageView.setImage(image);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
