package org.example.lsw_proto2.pve;

import java.util.function.Consumer;

public interface PVECampaign {
    void startCampaign();
    void nextRoom();
    void quit();
    void useItem();
    void viewParty();
    void levelUpUnit();

    void setOnRoomChanged(Consumer<Integer> onRoomChanged);
    void setOnQuit(Consumer<Integer> onQuit);
}
