package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharDatingBranchASelect.CharDatingBranchASelectReq;
import emu.nebula.proto.CharDatingBranchASelect.CharDatingBranchASelectResp;
import emu.nebula.util.Utils;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_dating_branch_a_select_req)
public class HandlerCharDatingBranchASelectReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharDatingBranchASelectReq.parseFrom(message);
        
        // Get dating game
        var game = session.getPlayer().getDatingManager().getGame();
        
        if (game == null) {
            return session.encodeMsg(NetMsgId.char_dating_branch_a_select_failed_ack);
        }
        
        // Select branch A
        game.selectDatingBranchA(req.getOptionId());
        
        // Build response
        var rsp = CharDatingBranchASelectResp.newInstance()
                .addAllBranchBOptionIds(game.getBranchOptionsB());
        
        // Add random events
        for (var events : game.getLandmark().getLandmarkEvents().values()) {
            var event = Utils.randomElement(events);
            rsp.addLandmarkEventIds(event.getId());
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_dating_branch_a_select_succeed_ack, rsp);
    }

}
