package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharGemUsePreset.CharGemUsePresetReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_gem_use_preset_req)
public class HandlerCharGemUsePresetReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharGemUsePresetReq.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getCharId());
        
        if (character == null) {
            return session.encodeMsg(NetMsgId.char_gem_use_preset_failed_ack);
        }
        
        // Use preset
        boolean success = character.setCurrentGemPreset(req.getPresetId());
        
        if (success == false) {
            return session.encodeMsg(NetMsgId.char_gem_use_preset_failed_ack);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_gem_use_preset_succeed_ack);
    }

}
