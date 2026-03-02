package org.example.lsw_proto2.pve;

public class ViewPartyCommand implements PVECommand {
    @Override
    public void execute(PVECampaign pve) {
        pve.viewParty();
    }
}
