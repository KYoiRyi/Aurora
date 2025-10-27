package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.star_tower_build_brief_list_get_req)
public class HandlerStarTowerBuildBriefListGetReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        return this.encodeMsg(NetMsgId.star_tower_build_brief_list_get_succeed_ack);
    }

}
