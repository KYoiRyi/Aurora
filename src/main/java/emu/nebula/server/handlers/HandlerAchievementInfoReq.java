package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.achievement_info_req)
public class HandlerAchievementInfoReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Build response
        var rsp = session.getPlayer().getAchievementManager().toProto();
        
        // Encode and send
        return session.encodeMsg(NetMsgId.achievement_info_succeed_ack, rsp);
    }

}
