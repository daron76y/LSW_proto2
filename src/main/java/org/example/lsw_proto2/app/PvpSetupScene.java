package org.example.lsw_proto2.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.persistence.UserProfile;

import java.util.List;

/**
 * PvP setup flow - two steps shown in the same scene, swapping content:
 *   STEP 1: Player 1 enters Player 2's username, then picks their own PvP party.
 *   STEP 2: Player 2 picks their PvP party (Player 1 looks away).
 * Once both parties are chosen, SceneManager launches the PvP battle.
 */
public class PvpSetupScene {
    private final VBox root;
    private final StackPane contentArea;  // swapped between step 1 and step 2

    public PvpSetupScene(SceneManager sceneManager) {
        // -----------------------------------------------------------------------
        // |                              Top bar                                |
        // -----------------------------------------------------------------------
        Button backBtn = new Button("← Back");
        backBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #aaaaaa;
            -fx-cursor: hand;
            -fx-font-size: 13px;
        """);
        backBtn.setOnAction(e -> sceneManager.showMainMenu());

        Label title = new Label("PvP Match Setup");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #e0e0e0;");

        HBox topBar = new HBox(backBtn, new Spacer(), title, new Spacer());
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 20, 8, 20));

        // -----------------------------------------------------------------------
        // |                          Content area                               |
        // -----------------------------------------------------------------------
        contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Start on step 1
        showStep1(sceneManager);

        root = new VBox(topBar, contentArea);
        root.setStyle("-fx-background-color: #1a1a1a;");
    }

    // -----------------------------------------------------------------------
    // |           Step 1 - pick opponent and choose party                   |
    // -----------------------------------------------------------------------

    private void showStep1(SceneManager sceneManager) {
        UserProfile player1 = sceneManager.getCurrentUser();

        Label p1Label = new Label("Player 1: " + player1.getUsername());
        p1Label.setFont(Font.font("Serif", FontWeight.BOLD, 16));
        p1Label.setStyle("-fx-text-fill: #4a9eff;");

        // Player 2 username field
        Label p2Title = new Label("Enter Player 2's username:");
        p2Title.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        TextField p2Field = new TextField();
        p2Field.setPromptText("Player 2 username");
        p2Field.setMaxWidth(300);
        applyFieldStyle(p2Field);

        // Player 1 party picker
        Label partyTitle = new Label("Player 1 - choose your PvP party:");
        partyTitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        ToggleGroup p1Toggle = new ToggleGroup();
        VBox p1PartyList = buildPartyPicker(player1.getPvpParties(), p1Toggle, "#4a9eff");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #cc3333; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button nextBtn = new Button("Next: Player 2 picks →");
        nextBtn.setPrefWidth(220);
        styleActionButton(nextBtn, "#3a7bd5");
        nextBtn.setOnAction(e -> {
            // Validate Player 2 username
            String p2Name = p2Field.getText().trim();
            if (p2Name.isBlank()) {
                errorLabel.setText("Please enter Player 2's username.");
                errorLabel.setVisible(true);
                return;
            }
            if (p2Name.equalsIgnoreCase(player1.getUsername())) {
                errorLabel.setText("Player 2 must be a different user.");
                errorLabel.setVisible(true);
                return;
            }
            var p2ProfileOpt = sceneManager.getUserRepo().getUserByName(p2Name);
            if (p2ProfileOpt.isEmpty()) {
                errorLabel.setText("No account found for \"" + p2Name + "\".");
                errorLabel.setVisible(true);
                return;
            }
            UserProfile player2 = p2ProfileOpt.get();
            if (player2.getPvpParties().isEmpty()) {
                errorLabel.setText(p2Name + " has no PvP parties yet. They must complete a PvE campaign first.");
                errorLabel.setVisible(true);
                return;
            }

            // Validate Player 1 party selection
            Toggle selected = p1Toggle.getSelectedToggle();
            if (selected == null) {
                errorLabel.setText("Please select your PvP party.");
                errorLabel.setVisible(true);
                return;
            }
            Party p1Party = (Party) selected.getUserData();

            // Advance to step 2
            showStep2(sceneManager, player1, player2, p1Party);
        });

        VBox card = buildCard(p1Label, p2Title, p2Field, partyTitle, p1PartyList, errorLabel, nextBtn);
        contentArea.getChildren().setAll(card);
    }

    // -----------------------------------------------------------------------
    // |                   Step 2 - player 2 picks party                     |
    // -----------------------------------------------------------------------

    private void showStep2(SceneManager sceneManager, UserProfile player1, UserProfile player2, Party p1Party) {
        Label handoffLabel = new Label("Hand the device to " + player2.getUsername());
        handoffLabel.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        handoffLabel.setStyle("-fx-text-fill: #e0a030;");

        Label p2Label = new Label("Player 2: " + player2.getUsername());
        p2Label.setFont(Font.font("Serif", FontWeight.BOLD, 16));
        p2Label.setStyle("-fx-text-fill: #e05050;");

        Label partyTitle = new Label("Player 2 - choose your PvP party:");
        partyTitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        ToggleGroup p2Toggle = new ToggleGroup();
        VBox p2PartyList = buildPartyPicker(player2.getPvpParties(), p2Toggle, "#e05050");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #cc3333; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button fightBtn = new Button("⚔  Start Battle!");
        fightBtn.setPrefWidth(220);
        styleActionButton(fightBtn, "#7b2020");
        fightBtn.setOnAction(e -> {
            Toggle selected = p2Toggle.getSelectedToggle();
            if (selected == null) {
                errorLabel.setText("Please select a PvP party.");
                errorLabel.setVisible(true);
                return;
            }
            Party p2Party = (Party) selected.getUserData();
            sceneManager.startPvpMatch(player1, p1Party, player2, p2Party);
        });

        VBox card = buildCard(handoffLabel, p2Label, partyTitle, p2PartyList, errorLabel, fightBtn);
        contentArea.getChildren().setAll(card);
    }

    // -----------------------------------------------------------------------
    // |                              Helpers                                |
    // -----------------------------------------------------------------------

    private VBox buildPartyPicker(List<Party> parties, ToggleGroup toggleGroup, String accentColor) {
        VBox box = new VBox(6);
        for (Party party : parties) {
            ToggleButton btn = new ToggleButton(
                    party.getName() + "  [" + party.getUnits().size() + " heroes  |  "
                            + "avg lv." + (party.getCumulativeLevels() / Math.max(1, party.getUnits().size())) + "]"
            );
            btn.setToggleGroup(toggleGroup);
            btn.setUserData(party);
            btn.setPrefWidth(Double.MAX_VALUE);
            btn.setStyle(unselectedPartyStyle());
            btn.selectedProperty().addListener((obs, was, now) ->
                    btn.setStyle(now ? selectedPartyStyle(accentColor) : unselectedPartyStyle())
            );
            box.getChildren().add(btn);
        }
        return box;
    }

    private VBox buildCard(javafx.scene.Node... nodes) {
        VBox card = new VBox(14, nodes);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(32));
        card.setMaxWidth(520);
        card.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 12;
            -fx-border-color: #444;
            -fx-border-radius: 12;
            -fx-border-width: 1;
        """);
        StackPane.setAlignment(card, Pos.CENTER);
        return card;
    }

    private static void applyFieldStyle(TextField field) {
        field.setStyle("""
            -fx-background-color: #3a3a3a;
            -fx-text-fill: #e0e0e0;
            -fx-prompt-text-fill: #666666;
            -fx-border-color: #555555;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
            -fx-padding: 6 10 6 10;
        """);
    }

    private static void styleActionButton(Button btn, String color) {
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10 20 10 20;
        """, color));
    }

    private static String unselectedPartyStyle() {
        return """
            -fx-background-color: #3a3a3a;
            -fx-text-fill: #cccccc;
            -fx-background-radius: 6;
            -fx-border-color: #555;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-cursor: hand;
            -fx-padding: 8;
        """;
    }

    private static String selectedPartyStyle(String accent) {
        return String.format("""
            -fx-background-color: #1a2a4a;
            -fx-text-fill: white;
            -fx-background-radius: 6;
            -fx-border-color: %s;
            -fx-border-radius: 6;
            -fx-border-width: 2;
            -fx-cursor: hand;
            -fx-padding: 8;
        """, accent);
    }

    public VBox getRoot() { return root; }

    /** Spacer utility */
    private static class Spacer extends Region {
        Spacer() { HBox.setHgrow(this, Priority.ALWAYS); }
    }
}