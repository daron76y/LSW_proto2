package org.example.lsw_proto2.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import org.example.lsw_proto2.core.Party;
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
public class GameScene extends ConsoleGameScene {
    private final Label campaignLabel;
    private final SceneManager sceneManager;
    private final Party party;
    private final int startRoom;
    private Runnable endCallBack;

    private Runnable onCampaignComplete = null;
    public void setOnCampaignComplete(Runnable callback) { this.onCampaignComplete = callback; }

    private Consumer<Integer> onQuit = null;
    public void setOnQuit(Consumer<Integer> onQuit) { this.onQuit = onQuit; }

    public GameScene(SceneManager sceneManager, Party party, int startRoom) {
        this(sceneManager, party, startRoom, buildCampaignLabel(party, startRoom));
    }

    private GameScene(SceneManager sceneManager, Party party, int startRoom, Label campaignLabel) {
        super(buildTopBar(campaignLabel), "#494033");
        this.sceneManager = sceneManager;
        this.party = party;
        this.startRoom = startRoom;
        this.campaignLabel = campaignLabel;
    }

    private static Label buildCampaignLabel(Party party, int startRoom) {
        Label label = new Label(party.getName() + "'s Campaign - Room " + startRoom + " / 30");
        label.setFont(Font.font("Serif", 15));
        label.setStyle("-fx-text-fill: #cccccc;");
        return label;
    }

    private static HBox buildTopBar(Label campaignLabel) {
        HBox topBar = new HBox(campaignLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 8, 16));
        topBar.setStyle("""
            -fx-background-color: #1e1e3a;
            -fx-border-color: #333355;
            -fx-border-width: 0 0 1 0;
        """);
        return topBar;
    }

    @Override
    protected Button buildSubmitButton() {
        Button btn = new Button("Submit");
        btn.setStyle("""
            -fx-background-color: #3a7bd5;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        return btn;
    }

    @Override
    protected void buildExtraBottomNodes(HBox bottomBar, TextField inputField, Button submitBtn, TextArea console) {
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
        bottomBar.getChildren().add(backBtn);

        // wire up the end-of-campaign UI disable logic
        this.endCallBack = () -> Platform.runLater(() -> {
            console.appendText("\n--- Campaign ended! ---\n");
            inputField.setDisable(true);
            submitBtn.setDisable(true);
            backBtn.setVisible(true);
            backBtn.setManaged(true);
        });
    }

    public void startGame() {
        Thread gameThread = new Thread(() -> {
            PVECampaign engine = new PVECampaignEngine(party, inputService, outputService, startRoom);
            engine.setOnRoomChanged(room -> Platform.runLater(() ->
                    campaignLabel.setText(party.getName() + "'s Campaign - Room " + room + " / 30")));
            engine.setOnQuit(room -> Platform.runLater(() -> {
                if (onQuit != null) onQuit.accept(room);
                Platform.runLater(sceneManager::showMainMenu);
            }));
            engine.startCampaign();

            if (endCallBack != null) endCallBack.run();

            if (onCampaignComplete != null) Platform.runLater(onCampaignComplete);
        }, "game-thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }
}