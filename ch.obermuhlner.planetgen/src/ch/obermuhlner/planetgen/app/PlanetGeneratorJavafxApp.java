package ch.obermuhlner.planetgen.app;

import java.util.Random;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.Planet.PlanetTextures;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlanetGeneratorJavafxApp extends Application {

	private ImageView diffuseImageView;
	private ImageView normalImageView;

	private PhongMaterial material;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Random Planet Generator");
        Group root = new Group();
        Scene scene = new Scene(root);
        
        BorderPane mainBorderPane = new BorderPane();
        root.getChildren().add(mainBorderPane);
        
        // tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        mainBorderPane.setCenter(tabPane);
        
        // 2D diffuse texture
        diffuseImageView = new ImageView();
        tabPane.getTabs().add(new Tab("2D Color", diffuseImageView));

        // 2D diffuse texture
        normalImageView = new ImageView();
        tabPane.getTabs().add(new Tab("2D Normal", normalImageView));

        // 3D planet
    	StackPane node3dContainer = new StackPane();
    	tabPane.getTabs().add(new Tab("3D", node3dContainer));
    	Group world = new Group();
		material = new PhongMaterial(Color.WHITE);
    	Node node3d = createNode3D(node3dContainer, world, material);
    	node3dContainer.getChildren().add(node3d);
    	
        // editor border pane
        BorderPane editBorderPane = new BorderPane();
        mainBorderPane.setTop(editBorderPane);
        
        // buttons in editor border pane
        VBox buttonBox = new VBox();
        buttonBox.setSpacing(4);
        editBorderPane.setRight(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(4));
        
        Button randomPlanetButton = new Button("Random Planet");
        buttonBox.getChildren().add(randomPlanetButton);
        randomPlanetButton.addEventHandler(ActionEvent.ACTION, event -> {
        	createRandomPlanet();
        });

        // initial run
    	createRandomPlanet();

    	primaryStage.setScene(scene);
        primaryStage.show();
	}

	private Node createNode3D(Region container, Group world, PhongMaterial material) {
        Sphere sphere = new Sphere();
		sphere.setMaterial(material);
        world.getChildren().add(sphere);
        sphere.setRotationAxis(Rotate.Y_AXIS);
        
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sphere.setRotate(sphere.getRotate() + 1.0);
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
		PlanetTextures planetTextures = createTextures();
		
		diffuseImageView.setImage(planetTextures.diffuseTexture);
		normalImageView.setImage(planetTextures.normalTexture);
		
		material.setDiffuseMap(planetTextures.diffuseTexture);
		material.setBumpMap(planetTextures.normalTexture);
		material.setSpecularMap(planetTextures.specularTexture);
	}
	
	private PlanetTextures createTextures() {
		Random random = new Random();
		
		PlanetGenerator planetGenerator = new PlanetGenerator();
		Planet planet = planetGenerator.createPlanet(random);
		
		int imageWidth = 1024;
		int imageHeight = 512;
		PlanetTextures textures = planet.getTextures(imageWidth, imageHeight);
		
		return textures;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
