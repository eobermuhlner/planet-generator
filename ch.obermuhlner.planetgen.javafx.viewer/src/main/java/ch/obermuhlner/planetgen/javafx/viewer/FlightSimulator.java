package ch.obermuhlner.planetgen.javafx.viewer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ch.obermuhlner.planetgen.planet.Planet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FlightSimulator {
	private static final double TURN_PER_MILLIS = 0.02;

	private static final double TRANSLATE_PER_MILLIS = 0.01;
	
	private Planet planet;
	private final Set<KeyCode> pressedKeyCodes = new HashSet<>();
	private PerspectiveCamera camera;

	private double shipRoll = 0;
	private double shipPitch = 0;
	private double shipYaw = 0;


	public FlightSimulator(Planet planet) {
		this.planet = planet;
	}
	
	public void start() {
		Stage stage = new Stage();
		stage.setTitle("Flight Simulator " + Arrays.toString(planet.planetData.seed));
		
    	StackPane container = new StackPane();
    	Scene scene = new Scene(container);
    	stage.setScene(scene);

		Group world = new Group();
		
        Box sphere = new Box();
    	PhongMaterial material = new PhongMaterial(Color.BLUE);
		sphere.setMaterial(material);
        world.getChildren().add(sphere);

        Group cameraGroup = new Group();
        world.getChildren().add(cameraGroup);
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
        		new Rotate(-20, Rotate.Y_AXIS),
        		new Rotate(-20, Rotate.X_AXIS),
        		new Translate(0, 0, -5)
        		);
        cameraGroup.getChildren().add(camera);

        SubScene subScene = new SubScene(world, 800, 600, false, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        subScene.heightProperty().bind(container.heightProperty());
        subScene.widthProperty().bind(container.widthProperty());
        
        scene.setOnKeyPressed(event -> {
        	pressedKeyCodes.add(event.getCode());
        });
        scene.setOnKeyReleased(event -> {
        	pressedKeyCodes.remove(event.getCode());
        });
    	container.getChildren().add(subScene);

		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (pressedKeyCodes.contains(KeyCode.W)) {
					shipTranslateZ(TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.S)) {
					shipTranslateZ(-TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.A)) {
					shipYaw += TURN_PER_MILLIS;
				}
				if (pressedKeyCodes.contains(KeyCode.D)) {
					shipYaw -= TURN_PER_MILLIS;
				}
				if (pressedKeyCodes.contains(KeyCode.Q)) {
					shipTranslateX(-TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.E)) {
					shipTranslateX(TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.R)) {
					shipTranslateY(-TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.F)) {
					shipTranslateY(TRANSLATE_PER_MILLIS);
				}
				if (pressedKeyCodes.contains(KeyCode.LEFT)) {
					shipRoll += TURN_PER_MILLIS;
				}
				if (pressedKeyCodes.contains(KeyCode.RIGHT)) {
					shipRoll -= TURN_PER_MILLIS;
				}
				if (pressedKeyCodes.contains(KeyCode.UP)) {
					shipPitch += TURN_PER_MILLIS;
				}
				if (pressedKeyCodes.contains(KeyCode.DOWN)) {
					shipPitch -= TURN_PER_MILLIS;
				}
				
				matrixRotateNode(camera, shipRoll, shipPitch, shipYaw);
			}
		}));
		timeline.playFromStart();

		stage.show();
	}

	private void shipTranslateX(double translate) {
		camera.setTranslateX(camera.getTranslateX() + translate);
        //camera.getTransforms().add(new Translate(translate, 0, 0));
	}

	private void shipTranslateY(double translate) {
		camera.setTranslateY(camera.getTranslateY() + translate);
        //camera.getTransforms().add(new Translate(0, translate, 0));
	}

	private void shipTranslateZ(double translate) {
		camera.setTranslateZ(camera.getTranslateZ() + translate);
        //camera.getTransforms().add(new Translate(0, 0, translate));
	}

	private void shipTurnX(double turn) {
        camera.getTransforms().add(new Rotate(turn, Rotate.X_AXIS));
	}

	private void shipTurnZ(double turn) {
        camera.getTransforms().add(new Rotate(turn, Rotate.Z_AXIS));
	}

	// http://stackoverflow.com/questions/30145414/rotate-a-3d-object-on-3-axis-in-javafx-properly
	private void matrixRotateNode(Node node, double roll, double pitch, double yaw){
	    double A11 = Math.cos(roll)*Math.cos(yaw);
	    double A12 = Math.cos(pitch)*Math.sin(roll)+Math.cos(roll)*Math.sin(pitch)*Math.sin(yaw);
	    double A13 = Math.sin(roll)*Math.sin(pitch)-Math.cos(roll)*Math.cos(pitch)*Math.sin(yaw);
	    double A21 = -Math.cos(yaw)*Math.sin(roll);
	    double A22 = Math.cos(roll)*Math.cos(pitch)-Math.sin(roll)*Math.sin(pitch)*Math.sin(yaw);
	    double A23 = Math.cos(roll)*Math.sin(pitch)+Math.cos(pitch)*Math.sin(roll)*Math.sin(yaw);
	    double A31 = Math.sin(yaw);
	    double A32 = -Math.cos(yaw)*Math.sin(pitch);
	    double A33 = Math.cos(pitch)*Math.cos(yaw);

	    double d = Math.acos((A11+A22+A33-1.0) / 2.0);
	    if (d != 0.0){
	        double den=2.0 * Math.sin(d);
	        Point3D p= new Point3D((A32-A23)/den,(A13-A31)/den,(A21-A12)/den);
	        node.setRotationAxis(p);
	        node.setRotate(Math.toDegrees(d));                    
	    }
	}
}
