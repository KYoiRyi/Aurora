package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharGemEquipGem.CharGemEquipGemReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_gem_equip_gem_req)
public class HandlerCharGemEquipGemReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharGemEquipGemReq.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getCharId());
        
        if (character == null) {
            return session.encodeMsg(NetMsgId.char_gem_equip_gem_failed_ack);
        }
        
        // Equip gem
        boolean success = character.equipGem(req.getPresetId(), req.getSlotId(), req.getGemIndex());
        
        if (success == false) {
            return session.encodeMsg(NetMsgId.char_gem_equip_gem_failed_ack);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_gem_equip_gem_succeed_ack);
    }

}
