package org.example.lsw_proto2.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.io.GUIInputService;
import org.example.lsw_proto2.io.GUIOutputService;
import org.example.lsw_proto2.pve.PVECampaignEngine;

/**
 * This is the JavaFX GUI application for our entire game project
 * Layout:
 *   TOP    - title label
 *   CENTER - TextArea (read only console log. all game output goes here)
 *   BOTTOM - TextField + Submit button (player types commands here)
 * The game engine runs on a background thread. The player types the same
 * commands they would type in the console (e.g. "next", "attack Goblin",
 * "cast \"Fireball\" Goblin", "buy", "leave").
 */
public class GameApp extends Application {
    @Override
    public void start(Stage stage) {
        //Title bar (top)
        Label titleLabel = new Label("Legends of Sword and Wand — PvE Campaign");
        HBox topBar = new HBox(titleLabel);
        topBar.setPadding(new Insets(8));

        //Console log (center)
        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        VBox.setVgrow(console, Priority.ALWAYS);

        //Input field + button (bottom)
        TextField inputField = new TextField();
        inputField.setPromptText("Type a command and press Enter…");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button submitBtn = new Button("Submit");
        HBox bottomBar = new HBox(8, inputField, submitBtn);
        bottomBar.setPadding(new Insets(8));

        //Root layout
        VBox root = new VBox(topBar, console, bottomBar);
        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("LSW");
        stage.setOnCloseRequest(e -> {Platform.exit(); System.exit(0);});
        stage.show();

        //Setup I/O services
        GUIInputService  inputService  = new GUIInputService();
        GUIOutputService outputService = new GUIOutputService(console);

        //Create submit handler - sends the typed line into the input service
        Runnable submit = () -> {
            String line = inputField.getText();
            inputField.clear();
            console.appendText("> " + line + "\n"); //echo the input to the console output
            inputService.submitInput(line);
        };

        //Link input field to the submit handler
        submitBtn.setOnAction(e -> submit.run());
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) submit.run();
        });

        //start the game on a background thread
        Thread gameThread = new Thread(() -> {
            Party party = buildParty();
            PVECampaignEngine engine = new PVECampaignEngine(party, inputService, outputService, 0);
            engine.startCampaign();
            Platform.runLater(() -> {
                console.appendText("\n--- Campaign ended. Close the window to exit. ---\n");
                inputField.setDisable(true);
                submitBtn.setDisable(true);
            });
        }, "game-thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    private Party buildParty() {
        Unit a1 = new Unit("GNS",   10, 1, 100, 100, HeroClass.ORDER);
        Unit a2 = new Unit("Navo",  10, 1, 100, 100, HeroClass.CHAOS);
        Unit a3 = new Unit("Aella", 10, 1, 100, 100, HeroClass.WARRIOR);
        Unit a4 = new Unit("Burle", 10, 1, 100, 100, HeroClass.MAGE);

        a1.addExperience(1000000000);
        a2.addExperience(1000000000);
        a3.addExperience(1000000000);
        a4.addExperience(1000000000);

        Party party = new Party("Guardians");
        party.setGold(500);
        party.addUnit(a1);
        party.addUnit(a2);
        party.addUnit(a3);
        party.addUnit(a4);
        return party;
    }

    public static void main(String[] args) {
        launch(args);
    }
}