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
 * Shown when a PvE campaign is completed (room 30 reached).
 * The player chooses to either:
 *   - SAVE  the party into their PvP roster (max 5). If the roster is full,
 *           they are first asked which existing party to replace.
 *   - DISCARD the party and return to the main menu
 * After saving, the campaign save entry is deleted from the user profile
 * and the player is returned to the main menu.
 */
public class CampaignCompleteScene {
    private final VBox root;

    public CampaignCompleteScene(SceneManager sceneManager, Party completedParty, int score) {
        UserProfile user = sceneManager.getCurrentUser();

        // -----------------------------------------------------------------------
        // |                               Header                                |
        // -----------------------------------------------------------------------
        Label trophy = new Label("🏆");
        trophy.setFont(Font.font(48));

        Label title = new Label("Campaign Complete!");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #f0c040;");

        Label subtitle = new Label("\"" + completedParty.getName() + "\" has finished all 30 rooms.");
        subtitle.setFont(Font.font("Serif", 15));
        subtitle.setStyle("-fx-text-fill: #aaaaaa;");

        Label scoreLabel = new Label("Final Score: " + score);
        scoreLabel.setFont(Font.font("Serif", FontWeight.BOLD, 20));
        scoreLabel.setStyle("-fx-text-fill: #f0c040;");

        // -----------------------------------------------------------------------
        // |                           Party summary                             |
        // -----------------------------------------------------------------------
        VBox unitList = new VBox(4);
        completedParty.getUnits().forEach(u -> {
            Label ul = new Label("• " + u.getName() + "  [" + u.getMainClass() + " Lv." + u.getLevel() + "]"
                    + "  ATK:" + u.getAttack() + "  DEF:" + u.getDefense()
                    + "  HP:" + u.getMaxHealth() + "  MP:" + u.getMaxMana());
            ul.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
            unitList.getChildren().add(ul);
        });

        // -----------------------------------------------------------------------
        // |                              Buttons                                |
        // -----------------------------------------------------------------------
        Button saveBtn = new Button("Save Party for PvP");
        saveBtn.setPrefWidth(200);
        saveBtn.setStyle("""
            -fx-background-color: #3a7bd5;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10;
        """);

        Button discardBtn = new Button("Discard & Return to Menu");
        discardBtn.setPrefWidth(200);
        discardBtn.setStyle("""
            -fx-background-color: #5a5a5a;
            -fx-text-fill: #cccccc;
            -fx-font-size: 13px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10;
        """);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #cc3333; -fx-font-size: 12px;");
        statusLabel.setVisible(false);

        HBox btnRow = new HBox(16, saveBtn, discardBtn);
        btnRow.setAlignment(Pos.CENTER);

        // -----------------------------------------------------------------------
        // |                           Replace party                             |
        // -----------------------------------------------------------------------
        Label replaceLabel = new Label("Your PvP roster is full. Choose a party to replace:");
        replaceLabel.setStyle("-fx-text-fill: #e0a030; -fx-font-size: 13px;");
        replaceLabel.setVisible(false);
        replaceLabel.setManaged(false);

        VBox replaceList = new VBox(6);
        replaceList.setVisible(false);
        replaceList.setManaged(false);

        // -----------------------------------------------------------------------
        // |                              Handlers                               |
        // -----------------------------------------------------------------------
        discardBtn.setOnAction(e -> {
            //delete the campaign save entry, don't touch savedParties
            String campaignName = completedParty.getName() + "'s Campaign";
            user.deleteCampaignByName(campaignName);
            //also remove the party from savedParties (campaign parties) if present
            user.getPartyByName(completedParty.getName()).ifPresent(user::deleteParty);
            sceneManager.getUserRepo().saveUser(user);
            sceneManager.showMainMenu();
        });

        saveBtn.setOnAction(e -> {
            List<Party> pvpParties = user.getPvpParties();

            if (pvpParties.size() < UserProfile.MAX_PVP_PARTIES) {
                //roster has space - save immediately
                commitSave(sceneManager, user, completedParty, -1);
            } else {
                //roster full - reveal the replace party picker
                saveBtn.setDisable(true);
                replaceLabel.setVisible(true);
                replaceLabel.setManaged(true);
                replaceList.getChildren().clear();

                for (int i = 0; i < pvpParties.size(); i++) {
                    final int slot = i;
                    Party existing = pvpParties.get(i);
                    Button replaceBtn = new Button(
                            existing.getName() + "  [" + existing.getUnits().size() + " heroes]"
                    );
                    replaceBtn.setPrefWidth(Double.MAX_VALUE);
                    replaceBtn.setStyle("""
                        -fx-background-color: #6a2020;
                        -fx-text-fill: white;
                        -fx-background-radius: 5;
                        -fx-cursor: hand;
                        -fx-padding: 8;
                    """);
                    replaceBtn.setOnAction(re -> commitSave(sceneManager, user, completedParty, slot));
                    replaceList.getChildren().add(replaceBtn);
                }

                replaceList.setVisible(true);
                replaceList.setManaged(true);
            }
        });

        // -----------------------------------------------------------------------
        // |                                Card                                 |
        // -----------------------------------------------------------------------
        VBox card = new VBox(14,
                trophy, title, subtitle, scoreLabel,
                new Separator(),
                unitList,
                new Separator(),
                btnRow, statusLabel,
                replaceLabel, replaceList
        );
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(580);
        card.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 12;
            -fx-border-color: #f0c040;
            -fx-border-radius: 12;
            -fx-border-width: 1;
        """);

        root = new VBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
    }

    /**
     * Performs the actual save and removes the party from campaign saves,
     * adds (or replaces) it in the PvP roster, saves, and returns to menu.
     *
     * @param slot  the roster index to replace, or -1 to append
     */
    private void commitSave(SceneManager sceneManager, UserProfile user, Party party, int slot) {
        //remove from campaign tracking
        String campaignName = party.getName() + "'s Campaign";
        user.deleteCampaignByName(campaignName);
        user.getPartyByName(party.getName()).ifPresent(user::deleteParty);

        //add to PvP roster
        if (slot < 0) user.addPvpParty(party);
        else user.replacePvpParty(slot, party);

        sceneManager.getUserRepo().saveUser(user);
        sceneManager.showMainMenu();
    }

    public VBox getRoot() { return root; }
}