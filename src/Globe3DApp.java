import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Globe3DApp extends Application {

    private static final double SCENE_SIZE = 600.0;

    private static final double GLOBE_RADIUS   = 250.0;
    private static final double EARTH_TILT_DEG = 23.5;

    private static final double MOON_RADIUS   = 60.0;
    private static final double MOON_DISTANCE = 400.0;
    private static final double MOON_ORBIT_INCLINATION_DEG = 5.0;

    private static final double EARTH_ROTATION_PERIOD_SEC = 8.0;
    private static final double MOON_ORBIT_PERIOD_SEC = 80.0;
    private static final double MOON_SELF_ROTATION_PERIOD_SEC = MOON_ORBIT_PERIOD_SEC;

    private static final double ISS_ORBIT_RADIUS = GLOBE_RADIUS * 1.07;
    private static final double ISS_ORBIT_INCLINATION_DEG = 51.6;
    private static final double ISS_ORBIT_PERIOD_SEC = 20.0;
    private static final double ISS_WIDTH  = 40.0;
    private static final double ISS_HEIGHT = 10.0;
    private static final double ISS_DEPTH  = 4.0;

    private static final double ZOOM_NEAR  = -400;
    private static final double ZOOM_FAR   = -2000;
    private static final double ZOOM_SPEED = 2.0;

    private static final double AUTO_ZOOM_DURATION_SEC = 5.0;
    private static final double AUTO_ZOOM_START_Z = -800.0;

    private double mouseOldX;
    private double mouseOldY;
    private double rotateXAngle = 0;
    private double rotateYAngle = 0;

    private double moonAngleDeg = 0.0;
    private double issAngleDeg = 0.0;

    private AnimationTimer autoZoomTimer;
    private boolean autoZoomFinished = false;

    @Override
    public void start(Stage primaryStage) {

        Sphere globe = new Sphere(GLOBE_RADIUS);

        PhongMaterial earthMat = new PhongMaterial();
        try {
            Image earthTexture = new Image(new FileInputStream("earth.jpg"));
            earthMat.setDiffuseMap(earthTexture);
        } catch (FileNotFoundException e) {
            System.err.println("WARNUNG: earth.jpg nicht gefunden – verwende nur Farbe.");
            earthMat.setDiffuseColor(Color.DARKCYAN);
        }
        globe.setMaterial(earthMat);

        Group earthGroup = new Group(globe);
        Rotate earthTilt = new Rotate(EARTH_TILT_DEG, Rotate.Z_AXIS);
        earthGroup.getTransforms().add(earthTilt);

        Rotate earthSpin = new Rotate(0, Rotate.Y_AXIS);
        globe.getTransforms().add(earthSpin);

        AnimationTimer earthTimer = new AnimationTimer() {
            private long lastNs = 0;
            private double angle = 0.0;

            @Override
            public void handle(long now) {
                if (lastNs == 0) {
                    lastNs = now;
                    return;
                }
                double deltaSec = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;
                double degPerSec = 360.0 / EARTH_ROTATION_PERIOD_SEC;
                angle = (angle + degPerSec * deltaSec) % 360.0;
                earthSpin.setAngle(angle);
            }
        };
        earthTimer.start();

        Sphere moon = new Sphere(MOON_RADIUS);

        PhongMaterial moonMat = new PhongMaterial();
        try {
            Image moonTexture = new Image(new FileInputStream("moon.jpg"));
            moonMat.setDiffuseMap(moonTexture);
        } catch (FileNotFoundException e) {
            System.err.println("INFO: moon.jpg nicht gefunden – verwende graue Farbe für den Mond.");
            moonMat.setDiffuseColor(Color.LIGHTGRAY);
        }
        moon.setMaterial(moonMat);

        Rotate moonSelfSpin = new Rotate(0, Rotate.Y_AXIS);
        moon.getTransforms().add(moonSelfSpin);

        AnimationTimer moonSpinTimer = new AnimationTimer() {
            private long lastNs = 0;
            private double angle = 0.0;

            @Override
            public void handle(long now) {
                if (lastNs == 0) {
                    lastNs = now;
                    return;
                }
                double deltaSec = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;
                double degPerSec = 360.0 / MOON_SELF_ROTATION_PERIOD_SEC;
                angle = (angle + degPerSec * deltaSec) % 360.0;
                moonSelfSpin.setAngle(angle);
            }
        };
        moonSpinTimer.start();

        Box iss = new Box(ISS_WIDTH, ISS_HEIGHT, ISS_DEPTH);

        PhongMaterial issMat = new PhongMaterial();
        try {
            Image issTexture = new Image(new FileInputStream("iss.png"));
            issMat.setDiffuseMap(issTexture);
        } catch (FileNotFoundException e) {
            System.err.println("INFO: iss.png nicht gefunden – verwende einfache Farbe für ISS.");
            issMat.setDiffuseColor(Color.SILVER);
        }
        iss.setMaterial(issMat);
        iss.setRotationAxis(Rotate.Y_AXIS);
        iss.setRotate(20);

        Group world = new Group(earthGroup, moon, iss);

        double moonOrbitIncRad = Math.toRadians(MOON_ORBIT_INCLINATION_DEG);

        AnimationTimer moonOrbitTimer = new AnimationTimer() {
            private long lastNs = 0;

            @Override
            public void handle(long now) {
                if (lastNs == 0) {
                    lastNs = now;
                    return;
                }
                double deltaSec = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;

                double degPerSec = 360.0 / MOON_ORBIT_PERIOD_SEC;
                moonAngleDeg = (moonAngleDeg + degPerSec * deltaSec) % 360.0;
                double rad = Math.toRadians(moonAngleDeg);

                double x0 = Math.cos(rad) * MOON_DISTANCE;
                double z0 = Math.sin(rad) * MOON_DISTANCE;

                double y  = -z0 * Math.sin(moonOrbitIncRad);
                double z  =  z0 * Math.cos(moonOrbitIncRad);

                moon.setTranslateX(x0);
                moon.setTranslateY(y);
                moon.setTranslateZ(z);
            }
        };
        moonOrbitTimer.start();

        double issOrbitIncRad = Math.toRadians(ISS_ORBIT_INCLINATION_DEG);

        AnimationTimer issOrbitTimer = new AnimationTimer() {
            private long lastNs = 0;

            @Override
            public void handle(long now) {
                if (lastNs == 0) {
                    lastNs = now;
                    return;
                }
                double deltaSec = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;

                double degPerSec = 360.0 / ISS_ORBIT_PERIOD_SEC;
                issAngleDeg = (issAngleDeg + degPerSec * deltaSec) % 360.0;
                double rad = Math.toRadians(issAngleDeg);

                double x0 = Math.cos(rad) * ISS_ORBIT_RADIUS;
                double z0 = Math.sin(rad) * ISS_ORBIT_RADIUS;

                double y  = -z0 * Math.sin(issOrbitIncRad);
                double z  =  z0 * Math.cos(issOrbitIncRad);

                iss.setTranslateX(x0);
                iss.setTranslateY(y);
                iss.setTranslateZ(z);
            }
        };
        issOrbitTimer.start();

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(4000.0);
        camera.setFieldOfView(30);
        camera.setTranslateZ(AUTO_ZOOM_START_Z);

        autoZoomTimer = new AnimationTimer() {
            private long startNs = -1;

            @Override
            public void handle(long now) {
                if (startNs < 0) {
                    startNs = now;
                }
                double elapsedSec = (now - startNs) / 1_000_000_000.0;
                double progress = Math.min(1.0, elapsedSec / AUTO_ZOOM_DURATION_SEC);

                double startZ = AUTO_ZOOM_START_Z;
                double targetZ = ZOOM_FAR;

                double newZ = startZ + (targetZ - startZ) * progress;
                camera.setTranslateZ(newZ);

                if (progress >= 1.0) {
                    autoZoomFinished = true;
                    stop();
                }
            }
        };
        autoZoomTimer.start();

        SubScene subScene = new SubScene(world, SCENE_SIZE, SCENE_SIZE, true,
                                         SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        BorderPane root = new BorderPane(subScene);
        Scene scene = new Scene(root, SCENE_SIZE, SCENE_SIZE, true);

        addInteraction(world, scene, camera);

        primaryStage.setTitle("Erde, Mond & ISS – JavaFX-Simulation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void addInteraction(Group world, Scene scene, PerspectiveCamera camera) {
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        world.getTransforms().addAll(rotateX, rotateY);

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double dx = event.getSceneX() - mouseOldX;
            double dy = event.getSceneY() - mouseOldY;

            double modifier = 0.5;

            rotateYAngle += dx * modifier;
            rotateXAngle -= dy * modifier;

            rotateX.setAngle(rotateXAngle);
            rotateY.setAngle(rotateYAngle);

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!autoZoomFinished && autoZoomTimer != null) {
                autoZoomFinished = true;
                autoZoomTimer.stop();
            }

            double delta = event.getDeltaY() * ZOOM_SPEED;
            double newZ = camera.getTranslateZ() + delta;

            if (newZ > ZOOM_NEAR) {
                newZ = ZOOM_NEAR;
            } else if (newZ < ZOOM_FAR) {
                newZ = ZOOM_FAR;
            }
            camera.setTranslateZ(newZ);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
