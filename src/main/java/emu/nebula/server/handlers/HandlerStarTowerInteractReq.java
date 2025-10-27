package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.StarTowerInteract.StarTowerInteractReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.star_tower_interact_req)
public class HandlerStarTowerInteractReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Get star tower instance
        var instance = session.getPlayer().getStarTowerManager().getInstance();
        
        if (instance == null) {
            return this.encodeMsg(NetMsgId.star_tower_interact_failed_ack);
        }
        
        // Parse request
        var req = StarTowerInteractReq.parseFrom(message);
        
        // Handle interaction
        var rsp = instance.handleInteract(req);
        
        // Template
        return this.encodeMsg(NetMsgId.star_tower_interact_succeed_ack, rsp);
    }

}
