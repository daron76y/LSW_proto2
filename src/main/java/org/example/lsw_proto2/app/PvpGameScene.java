package org.example.lsw_proto2.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.persistence.UserProfile;
import org.example.lsw_proto2.pvp.PvPMatch;
import org.example.lsw_proto2.pvp.PvPMatchEngine;

import java.util.List;

/**
 * Battle screen for PvP matches.
 * Looks just like GameScenes layout (console + input field) but runs PvPMatchEngine
 * instead of PVECampaignEngine. When the battle ends it:
 *   1) Records the win/loss on both user profiles
 *   2)Saves both parties back to their owners PvP roster
 *   3) Navigates to PvpResultsScene
 */
public class PvpGameScene extends ConsoleGameScene {
    private final SceneManager sceneManager;
    private final UserProfile player1, player2;
    private final Party p1Party, p2Party;

    public PvpGameScene(SceneManager sceneManager, UserProfile player1, Party p1Party, UserProfile player2, Party p2Party) {
        super(buildTopBar(player1, p1Party, player2, p2Party), "#1e1e2e");
        this.sceneManager = sceneManager;
        this.player1 = player1;
        this.player2 = player2;
        this.p1Party = p1Party;
        this.p2Party = p2Party;
    }

    private static HBox buildTopBar(UserProfile p1, Party p1Party, UserProfile p2, Party p2Party) {
        Label header = new Label("⚔  " + p1.getUsername() + " [" + p1Party.getName() + "]"
                + "   vs   "
                + p2.getUsername() + " [" + p2Party.getName() + "]");
        header.setFont(Font.font("Serif", 15));
        header.setStyle("-fx-text-fill: #cccccc;");
        HBox topBar = new HBox(header);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 16, 8, 16));
        topBar.setStyle("-fx-background-color: #1e1e3a; -fx-border-color: #333355; -fx-border-width: 0 0 1 0;");
        return topBar;
    }

    @Override
    protected Button buildSubmitButton() {
        Button btn = new Button("Submit");
        btn.setStyle("""
            -fx-background-color: #7b2020;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        return btn;
    }

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
                Stage stage = (Stage) root.getScene().getWindow();
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
}