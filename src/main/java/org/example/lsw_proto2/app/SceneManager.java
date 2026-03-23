package org.example.lsw_proto2.app;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.lsw_proto2.core.HeroClass;
import org.example.lsw_proto2.core.Items;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.persistence.InMemoryUserProfileRepo;
import org.example.lsw_proto2.persistence.MySQLUserProfileRepo;
import org.example.lsw_proto2.persistence.UserProfile;
import org.example.lsw_proto2.persistence.UserProfileRepository;

import java.util.Map;

/**
 * Central controller that owns the Stage and drives all scene transitions.
 *
 * Switching between repos ==================================================
 * Change USE_MYSQL to false to use the in-memory repo (no database required).
 * Change USE_MYSQL to true to persist data to a local MySQL server.
 */
public class SceneManager {
    private static final int WIDTH  = 900;
    private static final int HEIGHT = 650;

    //Change this one line to switch between repos
    private static final boolean USE_MYSQL = false;

    //MySQL credentials. Only used when USE_MYSQL is true
    private static final String MYSQL_USER     = "root";
    private static final String MYSQL_PASSWORD = "Bruh123!?"; //GUYS, set your local MySQL password here!!! and switch USE_MSQL to true

    private final Stage stage;
    private final UserProfileRepository userRepo;
    private UserProfile currentUser;

    public SceneManager(Stage stage) {
        this.stage = stage;
        stage.setTitle("Legends of Sword and Wand");
        stage.setOnCloseRequest(e -> System.exit(0));

        //use SQL if the switch has been set to true. Otherwise, use an InMemory repo
        userRepo = USE_MYSQL
                ? MySQLUserProfileRepo.connect(MYSQL_USER, MYSQL_PASSWORD)
                : new InMemoryUserProfileRepo();
    }

    // -----------------------------------------------------------------------
    // |                           Scene transitions                         |
    // -----------------------------------------------------------------------

    /** Show the login screen. Called on app start and on "log out". */
    public void showLogin() {
        LoginScene loginScene = new LoginScene(this);
        stage.setScene(new Scene(loginScene.getRoot(), WIDTH, HEIGHT));
        stage.show();
    }

    /** Called after successful login or account creation. */
    public void showMainMenu() {
        MainMenuScene mainMenuScene = new MainMenuScene(this, currentUser);
        stage.setScene(new Scene(mainMenuScene.getRoot(), WIDTH, HEIGHT));
    }

    /** Navigate to the new-campaign setup screen. */
    public void showNewCampaign() {
        NewCampaignScene newCampaignScene = new NewCampaignScene(this);
        stage.setScene(new Scene(newCampaignScene.getRoot(), WIDTH, HEIGHT));
    }

    /**
     * Launch a brand-new campaign with the given party and chosen class.
     * called from NewCampaignScene once the player confirms their hero.
     */
    public void startNewCampaign(String heroName, HeroClass heroClass, String partyName) {
        Party party = new Party(partyName);
        party.setGold(200);
        party.addUnit(new Unit(heroName, heroClass));

        // Save the party to the user profile so it shows up in the menu later
        currentUser.saveParty(party);
        currentUser.saveCampaign(partyName + "'s Campaign", partyName, 0);
        userRepo.saveUser(currentUser);

        showGame(party, 0);
    }

    /**
     * Resume an existing campaign.
     * called from MainMenuScene when the player clicks a saved campaign.
     */
    public void resumeCampaign(UserProfile.CampaignProgress progress) {
        Party party = currentUser.getPartyByName(progress.getPartyName())
                .orElseThrow(() -> new IllegalStateException("Saved party not found: " + progress.getPartyName()));
        showGame(party, progress.getCurrentRoom());
    }

    /** Show the campaign-complete save/discard screen (called when room 30 is reached). */
    public void showCampaignComplete(Party completedParty) {
        //calculate final score
        int score = completedParty.getUnits().stream().mapToInt(Unit::getLevel).sum() * 100;
        score += completedParty.getGold() * 10;
        for (Map.Entry<Items, Integer> entry : completedParty.getInventory().entrySet())
            score += (entry.getKey().getCost() / 2) * 10 * entry.getValue();

        //create the CampaignCompleteScene, with the score that was calculated
        CampaignCompleteScene scene = new CampaignCompleteScene(this, completedParty, score);
        stage.setScene(new Scene(scene.getRoot(), WIDTH, HEIGHT));
    }

    /** Launch the main game scene with the given party and starting room. */
    private void showGame(Party party, int startRoom) {
        GameScene gameScene = new GameScene(this, party, startRoom);
        stage.setScene(new Scene(gameScene.getRoot(), WIDTH, HEIGHT));

        //handle the quitting logic and save the progress to the user profile in the repo
        gameScene.setOnQuit(room -> {
            String campaignName = party.getName() + "'s Campaign";
            //update or create new campaign save entry
            if (currentUser.getCampaignByName(campaignName).isPresent()) { //update
                currentUser.updateCampaignRoom(campaignName, room);
            }
            else { //create
                if (currentUser.getPartyByName(party.getName()).isEmpty()) //save party if not already saved
                    currentUser.saveParty(party);
                currentUser.saveCampaign(campaignName, party.getName(), room);
            }
            userRepo.saveUser(currentUser); //save everything to the repo
        });

        //when the campaign completes, show the campaignComplete scene
        gameScene.setOnCampaignComplete(() -> showCampaignComplete(party));

        gameScene.startGame();
    }

    // -----------------------------------------------------------------------
    // |                          Scene transitions - PvP                    |
    // -----------------------------------------------------------------------

    /** Show the PvP setup screen (enter opponent username + pick parties). */
    public void showPvpSetup() {
        PvpSetupScene scene = new PvpSetupScene(this);
        stage.setScene(new Scene(scene.getRoot(), WIDTH, HEIGHT));
    }

    /**
     * Launch the PvP battle between two players.
     * called from PvpSetupScene once both parties are confirmed.
     */
    public void startPvpMatch(UserProfile player1, Party p1Party, UserProfile player2, Party p2Party) {
        PvpGameScene gameScene = new PvpGameScene(this, player1, p1Party, player2, p2Party);
        stage.setScene(new Scene(gameScene.getRoot(), WIDTH, HEIGHT));
        gameScene.startMatch();
    }

    // -----------------------------------------------------------------------
    // |                    Auth helpers (called by LoginScene)              |
    // -----------------------------------------------------------------------

    /**
     * Attempt to log in. Returns an error message on failure, or null on success.
     */
    public String login(String username, String password) {
        if (username.isBlank()) return "Username cannot be empty.";
        if (password.isBlank()) return "Password cannot be empty.";

        var profileOpt = userRepo.getUserByName(username);
        if (profileOpt.isEmpty()) return "No account found for \"" + username + "\".";
        if (!profileOpt.get().getPassword().equals(password)) return "Incorrect password.";

        currentUser = profileOpt.get();
        return null; // success
    }

    /**
     * Attempt to create a new account. Returns an error message on failure, or null on success.
     */
    public String createAccount(String username, String password) {
        if (username.isBlank()) return "Username cannot be empty.";
        if (password.length() < 4) return "Password must be at least 4 characters.";
        if (userRepo.exists(username)) return "Username \"" + username + "\" is already taken.";

        currentUser = new UserProfile(username, password);
        userRepo.saveUser(currentUser);
        return null; // success
    }

    public UserProfile getCurrentUser() { return currentUser; }
    public UserProfileRepository getUserRepo() { return userRepo; }
}