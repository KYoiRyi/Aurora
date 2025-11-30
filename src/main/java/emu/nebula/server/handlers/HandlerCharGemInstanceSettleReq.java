package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharGemInstanceSettle.CharGemInstanceSettleReq;
import emu.nebula.proto.CharGemInstanceSettle.CharGemInstanceSettleResp;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.game.instance.InstanceSettleData;
import emu.nebula.game.quest.QuestCondition;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_gem_instance_settle_req)
public class HandlerCharGemInstanceSettleReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Cache player
        var player = session.getPlayer();
        
        // Get boss level data
        var data = GameData.getCharGemInstanceDataTable().get(player.getInstanceManager().getCurInstanceId());
        if (data == null || !data.hasEnergy(player)) {
            return session.encodeMsg(NetMsgId.char_gem_instance_settle_failed_ack);
        }
        
        // Parse request
        var req = CharGemInstanceSettleReq.parseFrom(message);
        
        // Settle instance
        var changes = player.getInstanceManager().settleInstance(
                data,
                QuestCondition.CharGemInstanceClearTotal,
                player.getProgress().getCharGemLog(),
                "charGemLog",
                req.getStar()
        );
        
        var settleData = (InstanceSettleData) changes.getExtraData();
        
        // Handle client events for achievements
        session.getPlayer().getAchievementManager().handleClientEvents(req.getEvents());
        
        // Create response
        var rsp = CharGemInstanceSettleResp.newInstance()
                .setExp(settleData.getExp())
                .setThreeStar(req.getStar() == 7)
                .setChange(changes.toProto());
        
        // Add reward items
        if (settleData.getRewards() != null) {
            settleData.getRewards().toItemTemplateStream().forEach(rsp::addAwardItems);
        }
        if (settleData.getFirstRewards() != null) {
            settleData.getFirstRewards().toItemTemplateStream().forEach(rsp::addFirstItems);
        }
        
        // Send response
        return session.encodeMsg(NetMsgId.char_gem_instance_settle_succeed_ack, rsp);
    }

}
