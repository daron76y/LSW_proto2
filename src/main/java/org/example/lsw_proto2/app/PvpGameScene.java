package org.example.lsw_proto2.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.io.GUIInputService;
import org.example.lsw_proto2.io.GUIOutputService;
import org.example.lsw_proto2.persistence.UserProfile;
import org.example.lsw_proto2.pvp.PvPMatch;
import org.example.lsw_proto2.pvp.PvPMatchEngine;

import java.util.List;

/**
 * Battle screen for PvP matches.
 * Mirrors GameScenes layout (console + input field) but runs PvPMatchEngine
 * instead of PVECampaignEngine. When the battle ends it:
 *   1) Records the win/loss on both user profiles
 *   2)Saves both parties back to their owners PvP roster
 *   3) Navigates to PvpResultsScene
 */
public class PvpGameScene {
    private final VBox root;
    private final GUIInputService inputService;
    private final GUIOutputService outputService;

    private final SceneManager sceneManager;
    private final UserProfile player1;
    private final UserProfile player2;
    private final Party p1Party;
    private final Party p2Party;

    public PvpGameScene(SceneManager sceneManager, UserProfile player1, Party p1Party, UserProfile player2, Party p2Party) {
        this.sceneManager = sceneManager;
        this.player1 = player1;
        this.p1Party = p1Party;
        this.player2 = player2;
        this.p2Party = p2Party;

        // -----------------------------------------------------------------------
        // |                              Top bar                                |
        // -----------------------------------------------------------------------
        Label header = new Label("⚔  " + player1.getUsername() + " [" + p1Party.getName() + "]"
                + "   vs   "
                + player2.getUsername() + " [" + p2Party.getName() + "]");
        header.setFont(Font.font("Serif", 15));
        header.setStyle("-fx-text-fill: #cccccc;");

        HBox topBar = new HBox(header);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 8, 16));
        topBar.setStyle("-fx-background-color: #1e1e3a; -fx-border-color: #333355; -fx-border-width: 0 0 1 0;");

        // -----------------------------------------------------------------------
        // |                              Console                                |
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
        inputField.setPromptText("Type a command and press Enter…");
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
            -fx-background-color: #7b2020;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);

        HBox bottomBar = new HBox(8, inputField, submitBtn);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 16, 10, 16));
        bottomBar.setStyle("-fx-background-color: #1e1e2e;");

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
        inputField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) submit.run(); });

        root = new VBox(topBar, console, bottomBar);
        root.setStyle("-fx-background-color: #1a1a1a;");
    }

    /**
     * Starts the PvP battle on a background thread.
     * Must be called AFTER the scene has been set on the stage.
     */
    public void startMatch() {
        Thread battleThread = new Thread(() -> {
            PvPMatch engine = new PvPMatchEngine(p1Party, p2Party, inputService, outputService);
            Party winner = engine.startMatch();

            //Post-battle: update W/L, save parties, then show results
            boolean p1Won = winner.getName().equals(p1Party.getName());

            if (p1Won) { player1.addPvpWin();  player2.addPvpLoss(); }
            else       { player2.addPvpWin();  player1.addPvpLoss(); }

            // Save both parties back to their respective PvP roster slots
            savePartyBack(player1, p1Party);
            savePartyBack(player2, p2Party);

            sceneManager.getUserRepo().saveUser(player1);
            sceneManager.getUserRepo().saveUser(player2);

            Platform.runLater(() -> {
                PvpResultsScene results = new PvpResultsScene(sceneManager, player1, p1Party, player2, p2Party, winner);
                javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
                stage.setScene(new Scene(results.getRoot(), root.getScene().getWidth(), root.getScene().getHeight()));
            });
        }, "pvp-battle-thread");
        battleThread.setDaemon(true);
        battleThread.start();
    }

    /**
     * Replaces the party in the users PvP roster with the (now revived) post-battle version.
     * If the party is no longer in the roster, it is appended
     */
    private void savePartyBack(UserProfile user, Party party) {
        List<Party> roster = user.getPvpParties();
        for (int i = 0; i < roster.size(); i++) {
            if (roster.get(i).getName().equals(party.getName())) {
                user.replacePvpParty(i, party);
                return;
            }
        }
        //fallback: party not found in roster, so append if space allows
        if (roster.size() < UserProfile.MAX_PVP_PARTIES) user.addPvpParty(party);
    }

    public VBox getRoot() { return root; }
}