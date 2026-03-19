package org.example.lsw_proto2.app;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 *
 * GameApp is intentionally thin - it simply creates a SceneManager and
 * hands it the Stage. All scene construction and navigation lives in
 * SceneManager and the individual *Scene classes.
 */
public class GameApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager sceneManager = new SceneManager(stage);
        sceneManager.showLogin();
    }

    public static void main(String[] args) {launch(args);}
}