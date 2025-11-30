package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.SkillInstanceSettle.SkillInstanceSettleReq;
import emu.nebula.proto.SkillInstanceSettle.SkillInstanceSettleResp;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.game.instance.InstanceSettleData;
import emu.nebula.game.quest.QuestCondition;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.skill_instance_settle_req)
public class HandlerSkillInstanceSettleReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Cache player
        var player = session.getPlayer();
        
        // Get boss level data
        var data = GameData.getSkillInstanceDataTable().get(player.getInstanceManager().getCurInstanceId());
        if (data == null || !data.hasEnergy(player)) {
            return session.encodeMsg(NetMsgId.skill_instance_settle_failed_ack);
        }
        
        // Parse request
        var req = SkillInstanceSettleReq.parseFrom(message);
        
        // Settle instance
        var changes = player.getInstanceManager().settleInstance(
                data,
                QuestCondition.SkillInstanceClearTotal,
                player.getProgress().getSkillInstanceLog(),
                "skillInstanceLog",
                req.getStar()
        );
        
        var settleData = (InstanceSettleData) changes.getExtraData();
        
        // Handle client events for achievements
        session.getPlayer().getAchievementManager().handleClientEvents(req.getEvents());
        
        // Create response
        var rsp = SkillInstanceSettleResp.newInstance()
                .setExp(settleData.getExp())
                .setThreeStar(req.getStar() == 7)
                .setChange(changes.toProto());
        
        // Add reward items to proto
        if (settleData.getRewards() != null) {
            settleData.getRewards().toItemTemplateStream().forEach(rsp::addAwardItems);
        }
        if (settleData.getFirstRewards() != null) {
            settleData.getFirstRewards().toItemTemplateStream().forEach(rsp::addFirstItems);
        }
        
        // Send response
        return session.encodeMsg(NetMsgId.skill_instance_settle_succeed_ack, rsp);
    }

}
