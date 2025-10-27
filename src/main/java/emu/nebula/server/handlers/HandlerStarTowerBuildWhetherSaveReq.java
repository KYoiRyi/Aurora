package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.StarTowerBuildWhetherSave.StarTowerBuildWhetherSaveReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.star_tower_build_whether_save_req)
public class HandlerStarTowerBuildWhetherSaveReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        var req = StarTowerBuildWhetherSaveReq.parseFrom(message);
        
        // TODO
        return this.encodeMsg(NetMsgId.star_tower_build_whether_save_succeed_ack);
    }

}
