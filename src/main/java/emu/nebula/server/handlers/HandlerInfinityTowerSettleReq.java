package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.InfinityTowerSettle.InfinityTowerSettleReq;
import emu.nebula.proto.InfinityTowerSettle.InfinityTowerSettleResp;
import emu.nebula.net.HandlerId;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.infinity_tower_settle_req)
public class HandlerInfinityTowerSettleReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = InfinityTowerSettleReq.parseFrom(message);
        
        // Settle
        var manager = session.getPlayer().getInfinityTowerManager();
        var change = manager.settle(req.getValue());
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.infinity_tower_settle_failed_ack);
        }
        
        // Get next level
        int nextLevel = manager.getLevelId() + 1;
        
        // Try to apply for next level
        if (!manager.apply(nextLevel, -1)) {
            nextLevel = 0;
        }
        
        // Handle client events for achievements
        session.getPlayer().getAchievementManager().handleClientEvents(req.getEvents());
        
        // Build response
        var rsp = InfinityTowerSettleResp.newInstance()
                .setNextLevelId(nextLevel)
                .setBountyLevel(manager.getBountyLevel())
                .setChange(change.toProto());
        
        if (change.getExtraData() != null && change.getExtraData() instanceof ItemParamMap rewards) {
            rewards.toItemTemplateStream().forEach(rsp::addShow);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.infinity_tower_settle_succeed_ack, rsp);
    }

}
