package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharDatingLandmarkSelect.CharDatingLandmarkSelectReq;
import emu.nebula.proto.CharDatingLandmarkSelect.CharDatingLandmarkSelectResp;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_dating_landmark_select_req)
public class HandlerCharDatingLandmarkSelectReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse Request
        var req = CharDatingLandmarkSelectReq.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getCharId());
        
        if (character == null) {
            return session.encodeMsg(NetMsgId.char_dating_landmark_select_failed_ack);
        }
        
        // Set landmark
        var game = session.getPlayer().getDatingManager().selectLandmark(character, req.getLandmarkId());
        
        if (game == null) {
            return session.encodeMsg(NetMsgId.char_dating_landmark_select_failed_ack);
        }
        
        // Build response
        var rsp = CharDatingLandmarkSelectResp.newInstance()
                .setInfo(character.getAffinityProto())
                .addAllBranchAOptionIds(game.getBranchOptionsB());
        
        rsp.getMutableChange();
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_dating_landmark_select_succeed_ack, rsp);
    }

}
