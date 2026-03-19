package org.example.lsw_proto2.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.persistence.UserProfile;

/**
 * Results screen shown after a PvP battle ends.
 *
 * Displays:
 *   - Winner announcement
 *   - Both players' updated W/L records
 *   - "Back to Menu" button (returns to Player 1's main menu)
 */
public class PvpResultsScene {
    private final VBox root;

    public PvpResultsScene(SceneManager sceneManager, UserProfile player1, Party p1Party, UserProfile player2, Party p2Party, Party winningParty) {

        boolean p1Won = winningParty.getName().equals(p1Party.getName());
        UserProfile winner = p1Won ? player1 : player2;
        UserProfile loser  = p1Won ? player2 : player1;

        // -----------------------------------------------------------------------
        // |                               Winner                                |
        // -----------------------------------------------------------------------
        Label trophy = new Label("🏆");
        trophy.setFont(Font.font(52));

        Label winnerLabel = new Label(winner.getUsername() + " wins!");
        winnerLabel.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        winnerLabel.setStyle("-fx-text-fill: #f0c040;");

        Label partyLabel = new Label("with party: " + winningParty.getName());
        partyLabel.setFont(Font.font("Serif", 15));
        partyLabel.setStyle("-fx-text-fill: #aaaaaa;");

        // -----------------------------------------------------------------------
        // |                           Stats card                                |
        // -----------------------------------------------------------------------
        HBox statsRow = new HBox(20,
                buildStatsCard(player1, p1Party, p1Won),
                buildStatsCard(player2, p2Party, !p1Won)
        );
        statsRow.setAlignment(Pos.CENTER);

        // -----------------------------------------------------------------------
        // |                                Back                                 |
        // -----------------------------------------------------------------------
        Button backBtn = new Button("Back to Menu");
        backBtn.setPrefWidth(200);
        backBtn.setStyle("""
            -fx-background-color: #3a7bd5;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10 20 10 20;
        """);
        backBtn.setOnAction(e -> sceneManager.showMainMenu());

        // -----------------------------------------------------------------------
        // |                                Card                                 |
        // -----------------------------------------------------------------------
        VBox card = new VBox(16, trophy, winnerLabel, partyLabel, new Separator(), statsRow, backBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(640);
        card.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 12;
            -fx-border-color: #f0c040;
            -fx-border-radius: 12;
            -fx-border-width: 1;
        """);

        root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #494033;");
    }

    private VBox buildStatsCard(UserProfile user, Party party, boolean won) {
        String accentColor = won ? "#f0c040" : "#888888";
        String resultText  = won ? "VICTORY" : "DEFEAT";

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setFont(Font.font("Serif", FontWeight.BOLD, 16));
        usernameLabel.setStyle("-fx-text-fill: #e0e0e0;");

        Label partyLabel = new Label(party.getName());
        partyLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");

        Label resultLabel = new Label(resultText);
        resultLabel.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        resultLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        Label winsLabel = new Label("Wins:   " + user.getPvpWins());
        winsLabel.setStyle("-fx-text-fill: #88cc88; -fx-font-size: 13px;");

        Label lossesLabel = new Label("Losses: " + user.getPvpLosses());
        lossesLabel.setStyle("-fx-text-fill: #cc8888; -fx-font-size: 13px;");

        VBox card = new VBox(8, usernameLabel, partyLabel, new Separator(), resultLabel, winsLabel, lossesLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setPrefWidth(240);
        card.setStyle(String.format("""
            -fx-background-color: #333333;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-radius: 8;
            -fx-border-width: 1;
        """, accentColor));
        return card;
    }

    public VBox getRoot() { return root; }
}