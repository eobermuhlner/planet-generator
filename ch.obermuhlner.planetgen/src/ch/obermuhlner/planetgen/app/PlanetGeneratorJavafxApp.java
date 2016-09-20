package ch.obermuhlner.planetgen.app;

import java.util.Random;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.Planet.PlanetTextures;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlanetGeneratorJavafxApp extends Application {

	public static final double MAX_ANGLE = 2 * Math.PI;

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
        
        // diffuse texture 2D
        ImageView imageView = new ImageView();
        tabPane.getTabs().add(new Tab("2D", imageView));

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
        	createRandomPlanet(imageView);
        });

        // initial run
    	createRandomPlanet(imageView);

    	primaryStage.setScene(scene);
        primaryStage.show();
	}

	private void createRandomPlanet(ImageView imageView) {
		imageView.setImage(createTextures().diffuseTexture);
	}
	
	private PlanetTextures createTextures() {
		Random random = new Random();
		
		PlanetGenerator planetGenerator = new PlanetGenerator();
		Planet planet = planetGenerator.createPlanet(random);
		
		int imageSize = 1024;
		PlanetTextures textures = planet.getTextures(0, MAX_ANGLE, 0, MAX_ANGLE, imageSize, imageSize);
		
		return textures;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
