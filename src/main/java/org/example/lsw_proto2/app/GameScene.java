package org.example.lsw_proto2.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.io.GUIInputService;
import org.example.lsw_proto2.io.GUIOutputService;
import org.example.lsw_proto2.pve.PVECampaign;
import org.example.lsw_proto2.pve.PVECampaignEngine;

import java.util.function.Consumer;

/**
 * The main game screen.
 * Layout:
 *   TOP    - Campaign title bar
 *   CENTER - TextArea (console)
 *   BOTTOM - TextField + Submit button
 */
public class GameScene {
    private final VBox root;
    private final GUIInputService  inputService;
    private final GUIOutputService outputService;
    private final Party party;
    private final int startRoom;
    private final SceneManager sceneManager;
    private final Runnable endCallback;
    private final Label campaignLabel;

    //fired when the campaign finishes naturally (room 30 reached). SceneManager uses this to show the CampaignCompleteScene
    private Runnable onCampaignComplete = null;
    public void setOnCampaignComplete(Runnable callback) {this.onCampaignComplete = callback;}

    //similarly, for quitting and saving the campaign. SceneManager will use this to save progress to the repo
    private Consumer<Integer> onQuit = null;
    public void setOnQuit(Consumer<Integer> onQuit) {this.onQuit = onQuit;}

    public GameScene(SceneManager sceneManager, Party party, int startRoom) {
        this.sceneManager = sceneManager;
        this.party = party;
        this.startRoom = startRoom;

        // -----------------------------------------------------------------------
        // |                                Top bar                              |
        // -----------------------------------------------------------------------
        campaignLabel = new Label(party.getName() + "'s Campaign - Room " + startRoom + " / 30");
        campaignLabel.setFont(Font.font("Serif", 15));
        campaignLabel.setStyle("-fx-text-fill: #cccccc;");

        HBox topBar = new HBox(campaignLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 8, 16));
        topBar.setStyle("""
             -fx-background-color: #1e1e3a;
             -fx-border-color: #333355;
             -fx-border-width: 0 0 1 0;
        """);

        // -----------------------------------------------------------------------
        // |                                Console                              |
        // -----------------------------------------------------------------------
        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        console.setFont(Font.font("Monospaced", 13));
        console.setStyle("""
            -fx-control-inner-background: #1a1a1a;
            -fx-text-fill: #d0d0d0;
            -fx-border-color: transparent;
        """);
        VBox.setVgrow(console, Priority.ALWAYS);

        // -----------------------------------------------------------------------
        // |                            Input row                                |
        // -----------------------------------------------------------------------
        TextField inputField = new TextField();
        inputField.setPromptText("Type a command and press Enter!");
        inputField.setFont(Font.font("Monospaced", 13));
        inputField.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #e0e0e0;
            -fx-prompt-text-fill: #555555;
            -fx-border-color: #444444;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        """);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button submitBtn = new Button("Submit");
        submitBtn.setStyle("""
            -fx-background-color: #3a7bd5;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);

        // "Back to Menu" - only visible after campaign ends
        Button backBtn = new Button("< Back to Menu");
        backBtn.setVisible(false);
        backBtn.setManaged(false);
        backBtn.setStyle("""
            -fx-background-color: #555555;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        backBtn.setOnAction(e -> sceneManager.showMainMenu());

        HBox bottomBar = new HBox(8, inputField, submitBtn, backBtn);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 16, 10, 16));
        bottomBar.setStyle("-fx-background-color: #494033;");

        // -----------------------------------------------------------------------
        // |                          Submit Handler                             |
        // -----------------------------------------------------------------------
        inputService  = new GUIInputService();
        outputService = new GUIOutputService(console);
        Runnable submit = () -> {
            String line = inputField.getText();
            if (line.isBlank()) return;
            inputField.clear();
            console.appendText("> " + line + "\n");
            inputService.submitInput(line);
        };
        submitBtn.setOnAction(e -> submit.run());
        inputField.setOnKeyPressed(e -> {if (e.getCode() == KeyCode.ENTER) submit.run();});

        // -----------------------------------------------------------------------
        // |                                Root                                 |
        // -----------------------------------------------------------------------
        root = new VBox(topBar, console, bottomBar);
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Store UI refs needed in startGame()
        this.endCallback = () -> Platform.runLater(() -> {
            console.appendText("\n--- Campaign ended! ---\n");
            inputField.setDisable(true);
            submitBtn.setDisable(true);
            backBtn.setVisible(true);
            backBtn.setManaged(true);
        });
    }



    /**
     * Starts the PVECampaignEngine on a background thread.
     * Must be called AFTER the scene has been set on the stage.
     */
    public void startGame() {
        Thread gameThread = new Thread(() -> {
            PVECampaign engine = new PVECampaignEngine(party, inputService, outputService, startRoom);

            //room changed callback (allows the game logic to update the gui room counter)
            engine.setOnRoomChanged(room -> Platform.runLater(() -> {
                campaignLabel.setText(party.getName() + "'s Campaign - Room " + room + " / 30");
            }));

            //on quit callback (allows game logic to quit the game on a GUI-level
            engine.setOnQuit(room -> Platform.runLater(() -> {
                if (onQuit != null) onQuit.accept(room);
                Platform.runLater(sceneManager::showMainMenu);
            }));

            //start the actual pve campaign itself
            engine.startCampaign();

            //startCampaign() returns here when the campaign ends naturally, after reaching room 30
            if (onCampaignComplete != null) Platform.runLater(onCampaignComplete);
        }, "game-thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    public VBox getRoot() { return root; }
}