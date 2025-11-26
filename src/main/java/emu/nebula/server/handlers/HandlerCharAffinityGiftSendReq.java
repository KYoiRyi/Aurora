package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharAffinityGiftSend.CharAffinityGiftSendReq;
import emu.nebula.proto.CharAffinityGiftSend.CharAffinityGiftSendResp;
import emu.nebula.net.HandlerId;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_affinity_gift_send_req)
public class HandlerCharAffinityGiftSendReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharAffinityGiftSendReq.parseFrom(message);
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(req.getCharId());
        if (character == null) {
            return session.encodeMsg(NetMsgId.char_affinity_gift_send_failed_ack);
        }
        
        // Parse item templates
        var items = ItemParamMap.fromTemplates(req.getItems());
        
        // Send gifts
        var change = character.sendGift(items);
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.char_affinity_gift_send_failed_ack);
        }
        
        // Build response
        var rsp = CharAffinityGiftSendResp.newInstance()
                .setChange(change.toProto())
                .setInfo(character.getAffinityProto());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_affinity_gift_send_succeed_ack, rsp);
    }

}
