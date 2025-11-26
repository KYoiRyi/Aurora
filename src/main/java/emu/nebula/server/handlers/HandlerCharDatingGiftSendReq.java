package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharDatingGiftSend.CharDatingGiftSendReq;
import emu.nebula.proto.CharDatingGiftSend.CharDatingGiftSendResp;
import emu.nebula.net.HandlerId;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_dating_gift_send_req)
public class HandlerCharDatingGiftSendReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse Request
        var req = CharDatingGiftSendReq.parseFrom(message);
        
        // Get dating game
        var game = session.getPlayer().getDatingManager().getGame();
        
        if (game == null) {
            return session.encodeMsg(NetMsgId.char_dating_gift_send_failed_ack);
        }
        
        // Get character
        var character = game.getCharacter();
        
        if (character == null || character.getCharId() != req.getCharId()) {
            return session.encodeMsg(NetMsgId.char_dating_gift_send_failed_ack);
        }
        
        // Parse item templates
        var items = ItemParamMap.fromTemplates(req.getItems());
        
        // Send gifts
        var change = character.sendGift(items);
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.char_affinity_gift_send_failed_ack);
        }
        
        // Build response
        var rsp = CharDatingGiftSendResp.newInstance()
                .setChange(change.toProto())
                .setInfo(character.getAffinityProto());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.char_dating_gift_send_succeed_ack, rsp);
    }

}
