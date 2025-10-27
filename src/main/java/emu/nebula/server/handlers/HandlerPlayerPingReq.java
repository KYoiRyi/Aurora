package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.PlayerPing.Pong;
import emu.nebula.net.HandlerId;
import emu.nebula.Nebula;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.player_ping_req)
public class HandlerPlayerPingReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        var rsp = Pong.newInstance()
                .setServerTs(Nebula.getCurrentTime());
        
        return this.encodeMsg(NetMsgId.player_ping_succeed_ack, rsp);
    }

}
