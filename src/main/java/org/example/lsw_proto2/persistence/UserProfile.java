package org.example.lsw_proto2.persistence;

import org.example.lsw_proto2.core.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserProfile {
    public static class CampaignProgress {
        private String campaignName;
        private int currentRoom;
        private String partyName;

        public CampaignProgress() {} //Jackson no-arg constructor

        public CampaignProgress(String name, String partyName, int currentRoom) {
            this.campaignName = name;
            this.partyName = partyName;
            this.currentRoom = currentRoom;
        }

        public String getCampaignName() {return campaignName;}
        public String getPartyName() {return partyName;}
        public int getCurrentRoom() {return currentRoom;}
        public void setCurrentRoom(int currentRoom) {this.currentRoom = currentRoom;}
    }

    private final String username;
    private final String password;
    private int score;
    List<Party> savedParties;
    List<CampaignProgress> campaignSaves;

    public UserProfile() {
        this.username = null;
        this.password = null;
    } //for Jackson

    public UserProfile(String username, String password) {
        this.username = username;
        this.password = password;
        score = 0;
        savedParties = new ArrayList<>();
        campaignSaves = new ArrayList<>();
    }

    public String getUsername() {return username;}
    public String getPassword() {return password;}

    //scores======================================
    public int getScore() {return score;}
    public void increaseScore(int score) {
        this.score += score;
    }

    //saving parties =============================
    public void saveParty(Party party) {
        //ensure duplicate names dont exist
        if (getPartyByName(party.getName()).isPresent())
            throw new IllegalStateException("Party already saved for this user: " + party.getName());

        savedParties.add(party);
    }
    public void deleteParty(Party party) {
        savedParties.remove(party);
        //remove campaigns referencing this party
        campaignSaves.removeIf(c -> c.getPartyName().equals(party.getName()));
    }
    public List<Party> getSavedParties() {return savedParties;}
    public Optional<Party> getPartyByName(String partyName) {
        return savedParties.stream()
                .filter(p -> p.getName().equals(partyName))
                .findFirst();
    }

    //saving campaigns ============================
    public void saveCampaign(String campaignName, String partyName, int currentRoom) {
        //ensure the party is first already saved
        if (getPartyByName(partyName).isEmpty())
            throw new IllegalArgumentException("Party " + partyName + " does not exist for this user!");

        //overwrite campaign saves if they have the same name
        deleteCampaignByName(campaignName);
        campaignSaves.add(new CampaignProgress(campaignName, partyName, currentRoom));
    }
    public void deleteCampaignByName(String name) {
        campaignSaves.removeIf(c -> c.getCampaignName().equals(name));
    }
    public List<CampaignProgress> getCampaignSaves() {return campaignSaves;}
    public Optional<CampaignProgress> getCampaignByName(String campaignName) {
        return campaignSaves.stream()
                .filter(c -> c.getCampaignName().equals(campaignName))
                .findFirst();
    }
    public void updateCampaignRoom(String campaignName, int newRoom) {
        getCampaignByName(campaignName).ifPresent(c -> c.setCurrentRoom(newRoom));
    }
}
