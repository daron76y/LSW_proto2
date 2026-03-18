package org.example.lsw_proto2.pve;

public class LevelUpCommand implements PVECommand {
    @Override
    public void execute(PVECampaign pve) {
        pve.levelUpUnit();
    }
}
