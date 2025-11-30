package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.VampireSurvivorAreaChange.VampireSurvivorAreaChangeReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.vampire_survivor_area_change_req)
public class HandlerVampireSurvivorAreaChangeReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse req
        var req = VampireSurvivorAreaChangeReq.parseFrom(message);
        
        // Get vampire survivor game
        var game = session.getPlayer().getVampireSurvivorManager().getGame();
        
        if (game == null) {
            session.encodeMsg(NetMsgId.vampire_survivor_area_change_failed_ack);
        }
        
        // Calculate score for area
        game.settleArea(req.getTime(), req.getKillCount().toArray());
        
        // Handle client events for achievements
        session.getPlayer().getAchievementManager().handleClientEvents(req.getEvents());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.vampire_survivor_area_change_succeed_ack);
    }

}
