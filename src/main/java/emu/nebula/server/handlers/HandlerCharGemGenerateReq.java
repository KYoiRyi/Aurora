package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharGemGenerate.CharGemGenerateReq;
import emu.nebula.proto.CharGemGenerate.CharGemGenerateResp;
import emu.nebula.net.HandlerId;
import emu.nebula.game.character.CharacterGem;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_gem_generate_req)
public class HandlerCharGemGenerateReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharGemGenerateReq.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getCharId());
        if (character == null) {
            return session.encodeMsg(NetMsgId.char_gem_generate_failed_ack);
        }
        
        // Generate gem
        var change = character.generateGem(req.getSlotId());
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.char_gem_generate_failed_ack);
        }
        
        var gem = (CharacterGem) change.getExtraData();
        
        // Build response
        var rsp = CharGemGenerateResp.newInstance()
                .setChangeInfo(change.toProto())
                .setCharGem(gem.toProto());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_gem_generate_succeed_ack, rsp);
    }

}
