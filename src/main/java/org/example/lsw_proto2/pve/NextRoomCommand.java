package org.example.lsw_proto2.pve;

public class NextRoomCommand implements PVECommand {
    @Override
    public void execute(PVECampaign pve) {
        pve.nextRoom();
    }
}
