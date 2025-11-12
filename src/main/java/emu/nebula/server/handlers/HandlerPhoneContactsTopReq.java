package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.Public.UI32;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.phone_contacts_top_req)
public class HandlerPhoneContactsTopReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = UI32.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getValue());
        if (character == null) {
            return session.encodeMsg(NetMsgId.phone_contacts_top_failed_ack);
        }
        
        // Toggle chat status
        character.getContact().toggleTop();
        
        // Encode and send
        return session.encodeMsg(NetMsgId.phone_contacts_top_succeed_ack);
    }

}
