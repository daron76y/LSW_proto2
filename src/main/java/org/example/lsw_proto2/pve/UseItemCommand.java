package org.example.lsw_proto2.pve;

public class UseItemCommand implements PVECommand {
    @Override
    public void execute(PVECampaign pve) {
        pve.useItem();
    }
}
