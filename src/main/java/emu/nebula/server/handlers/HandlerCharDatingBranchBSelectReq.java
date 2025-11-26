package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharDatingBranchBSelect.CharDatingBranchBSelectReq;
import emu.nebula.proto.CharDatingBranchBSelect.CharDatingBranchBSelectResp;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_dating_branch_b_select_req)
public class HandlerCharDatingBranchBSelectReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharDatingBranchBSelectReq.parseFrom(message);
        
        // Get dating game
        var game = session.getPlayer().getDatingManager().getGame();
        
        if (game == null) {
            return session.encodeMsg(NetMsgId.char_dating_branch_b_select_failed_ack);
        }
        
        // Select branch B
        game.selectDatingBranchB(req.getOptionId());
        
        // Build response
        var rsp = CharDatingBranchBSelectResp.newInstance()
                .setAfterBranchId(game.getLandmark().getRandomAfterBranchId())
                .setCharacterEventId(game.getLandmark().getRandomCharacterEventId());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_dating_branch_b_select_succeed_ack, rsp);
    }

}
