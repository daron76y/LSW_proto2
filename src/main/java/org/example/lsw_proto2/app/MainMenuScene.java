package org.example.lsw_proto2.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.lsw_proto2.persistence.UserProfile;

import java.util.List;

/**
 * Main menu shown after login.
 *
 * Layout:
 *   TOP    - "Welcome, [username]!" + Log Out button
 *   LEFT   - Saved Campaigns list (each row has a "Resume" button)
 *            + "New Campaign" button at the bottom
 *   RIGHT  - PvP section
 */
public class MainMenuScene {
    private final VBox root;

    public MainMenuScene(SceneManager sceneManager, UserProfile user) {
        // -----------------------------------------------------------------------
        // |                              Top bar                                |
        // -----------------------------------------------------------------------
        Label welcome = new Label("Welcome back, " + user.getUsername() + "!");
        welcome.setFont(Font.font("Serif", FontWeight.BOLD, 20));
        welcome.setStyle("-fx-text-fill: #e0e0e0;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle("""
            -fx-background-color: #5a5a5a;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        logoutBtn.setOnAction(e -> sceneManager.showLogin());

        HBox topBar = new HBox(welcome, new Spacer(), logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 20, 10, 20));

        // -----------------------------------------------------------------------
        // |                    saved campaigns left panel                       |
        // -----------------------------------------------------------------------
        Label campaignsTitle = new Label("Your Campaigns");
        campaignsTitle.setFont(Font.font("Serif", FontWeight.BOLD, 16));
        campaignsTitle.setStyle("-fx-text-fill: #cccccc;");

        VBox campaignList = new VBox(8);
        campaignList.setPadding(new Insets(8, 0, 8, 0));

        List<UserProfile.CampaignProgress> saves = user.getCampaignSaves();
        if (saves.isEmpty()) {
            Label empty = new Label("No saved campaigns yet. Start a new one!");
            empty.setStyle("-fx-text-fill: #888888;");
            campaignList.getChildren().add(empty);
        } else {
            for (UserProfile.CampaignProgress save : saves) {
                campaignList.getChildren().add(buildCampaignRow(save, sceneManager));
            }
        }

        ScrollPane campaignScroll = new ScrollPane(campaignList);
        campaignScroll.setFitToWidth(true);
        campaignScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(campaignScroll, Priority.ALWAYS);

        Button newCampaignBtn = new Button("+ New Campaign");
        newCampaignBtn.setPrefWidth(Double.MAX_VALUE);
        newCampaignBtn.setStyle("""
            -fx-background-color: #3a7bd5;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10;
        """);
        newCampaignBtn.setOnAction(e -> sceneManager.showNewCampaign());

        VBox leftPanel = new VBox(12, campaignsTitle, campaignScroll, newCampaignBtn);
        leftPanel.setPadding(new Insets(16));
        leftPanel.setPrefWidth(260);
        leftPanel.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 10;
            -fx-border-color: #444;
            -fx-border-radius: 10;
            -fx-border-width: 1;
        """);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // -----------------------------------------------------------------------
        // |                         pPvp right panel                            |
        // -----------------------------------------------------------------------
        Label pvpTitle = new Label("PvP Battle");
        pvpTitle.setFont(Font.font("Serif", FontWeight.BOLD, 16));
        pvpTitle.setStyle("-fx-text-fill: #cccccc;");

        Label pvpDesc = new Label("Challenge another player to a head-to-head battle on the same device.");
        pvpDesc.setWrapText(true);
        pvpDesc.setStyle("-fx-text-fill: #888888;");

        // W/L record
        Label wlLabel = new Label("Record:  " + user.getPvpWins() + "W  /  " + user.getPvpLosses() + "L");
        wlLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        // PvP party count
        int pvpPartyCount = user.getPvpParties().size();
        Label pvpPartyCountLabel = new Label("PvP parties: " + pvpPartyCount + " / " + UserProfile.MAX_PVP_PARTIES);
        pvpPartyCountLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        boolean canStartPvp = pvpPartyCount > 0;
        Button pvpBtn = new Button(canStartPvp ? "Find Opponent" : "No PvP Parties Yet");
        pvpBtn.setPrefWidth(Double.MAX_VALUE);
        pvpBtn.setDisable(!canStartPvp);
        pvpBtn.setStyle(canStartPvp ? """
            -fx-background-color: #7b2020;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            -fx-padding: 10;
        """ : """
            -fx-background-color: #444444;
            -fx-text-fill: #888888;
            -fx-font-size: 13px;
            -fx-background-radius: 6;
            -fx-padding: 10;
        """);
        if (canStartPvp) pvpBtn.setOnAction(e -> sceneManager.showPvpSetup());

        Label pvpHint = new Label(canStartPvp ? "" : "Complete a PvE campaign to unlock PvP.");
        pvpHint.setWrapText(true);
        pvpHint.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox rightPanel = new VBox(12, pvpTitle, pvpDesc, wlLabel, pvpPartyCountLabel, new Spacer(), pvpHint, pvpBtn);
        rightPanel.setPadding(new Insets(16));
        rightPanel.setPrefWidth(260);
        rightPanel.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-background-radius: 10;
            -fx-border-color: #444;
            -fx-border-radius: 10;
            -fx-border-width: 1;
        """);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // -----------------------------------------------------------------------
        // |                           Main content row                          |
        // -----------------------------------------------------------------------
        HBox content = new HBox(16, leftPanel, rightPanel);
        content.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(content, Priority.ALWAYS);

        // -----------------------------------------------------------------------
        // |                                root                                 |
        // -----------------------------------------------------------------------
        root = new VBox(topBar, content);
        root.setStyle("-fx-background-color: #1a1a1a;");
    }

    /** Build one row in the saved-campaign list. */
    private HBox buildCampaignRow(UserProfile.CampaignProgress save, SceneManager sceneManager) {
        Label nameLabel = new Label(save.getCampaignName());
        nameLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

        Label roomLabel = new Label("Room " + save.getCurrentRoom() + " / 30");
        roomLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        VBox info = new VBox(2, nameLabel, roomLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button resumeBtn = new Button("Resume");
        resumeBtn.setStyle("""
            -fx-background-color: #2e7d32;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
        resumeBtn.setOnAction(e -> sceneManager.resumeCampaign(save));

        HBox row = new HBox(12, info, resumeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("""
            -fx-background-color: #3a3a3a;
            -fx-background-radius: 6;
        """);

        return row;
    }

    public VBox getRoot() { return root; }

    /** Spacer utility - fills remaining horizontal/vertical space in an HBox/VBox. */
    private static class Spacer extends Region {
        Spacer() {
            HBox.setHgrow(this, Priority.ALWAYS);
            VBox.setVgrow(this, Priority.ALWAYS);
        }
    }
}