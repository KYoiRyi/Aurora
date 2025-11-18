package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.DiscAllLimitBreak.DiscAllLimitBreakResp;
import emu.nebula.proto.DiscAllLimitBreak.DiscLimitBreakChange;
import emu.nebula.net.HandlerId;

import java.util.List;

import emu.nebula.game.character.GameDisc;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.disc_all_limit_break_req)
public class HandlerDiscAllLimitBreakReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Limit break all discs
        var change = session.getPlayer().getCharacters().limitBreakAllDiscs();
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.disc_all_limit_break_failed_ack);
        }
        
        // Get changed discs
        @SuppressWarnings("unchecked")
        var discs = (List<GameDisc>) change.getExtraData();
        
        // Build response
        var rsp = DiscAllLimitBreakResp.newInstance()
                .setChange(change.toProto());
        
        for (var disc : discs) {
            var info = DiscLimitBreakChange.newInstance()
                    .setId(disc.getDiscId())
                    .setStar(disc.getStar());
            
            rsp.addLimitBreaks(info);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.disc_all_limit_break_succeed_ack, rsp);
    }

}
