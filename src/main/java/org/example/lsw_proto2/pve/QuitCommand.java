package org.example.lsw_proto2.pve;

public class QuitCommand implements PVECommand {
    @Override
    public void execute(PVECampaign pve) {
        pve.quit();
    }
}
