package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.ScoreBossSettle.ScoreBossSettleReq;
import emu.nebula.proto.ScoreBossSettle.ScoreBossSettleResp;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.score_boss_settle_req)
public class HandlerScoreBossSettleReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = ScoreBossSettleReq.parseFrom(message);
        
        // Settle
        boolean success = session.getPlayer().getScoreBossManager().settle(req.getStar(), req.getScore());
        
        if (success == false) {
            return session.encodeMsg(NetMsgId.score_boss_settle_failed_ack);
        }
        
        // Handle client events for achievements
        session.getPlayer().getAchievementManager().handleClientEvents(req.getEvents());
        
        // Build response
        var rsp = ScoreBossSettleResp.newInstance();
        
        // Encode and send
        return session.encodeMsg(NetMsgId.score_boss_settle_succeed_ack, rsp);
    }

}
