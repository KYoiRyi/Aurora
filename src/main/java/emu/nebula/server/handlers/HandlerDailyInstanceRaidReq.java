package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.DailyInstanceRaid.DailyInstanceRaidReq;
import emu.nebula.proto.DailyInstanceRaid.DailyInstanceRaidResp;
import emu.nebula.proto.DailyInstanceRaid.DailyInstanceReward;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.game.quest.QuestCondType;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.daily_instance_raid_req)
public class HandlerDailyInstanceRaidReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = DailyInstanceRaidReq.parseFrom(message);
        
        // Get instance data
        var data = GameData.getDailyInstanceDataTable().get(req.getId());
        if (data == null) {
            return session.encodeMsg(NetMsgId.daily_instance_raid_failed_ack);
        }
        
        // Check reward group
        if (data.getRewardGroup(req.getRewardType()) == null) {
            return session.encodeMsg(NetMsgId.daily_instance_raid_failed_ack);
        }
        
        // Sweep
        var change = session.getPlayer().getInstanceManager().sweepInstance(
                data,
                QuestCondType.DailyInstanceClearTotal,
                session.getPlayer().getInstanceManager().getDailyInstanceLog(),
                req.getRewardType(),
                req.getTimes()
        );
        
        // Sanity check
        if (change == null) {
            return session.encodeMsg(NetMsgId.daily_instance_raid_failed_ack);
        }
        
        // Build response
        var rsp = DailyInstanceRaidResp.newInstance()
                .setChange(change.toProto());
        
        // Cache reward list
        var rewardList = data.getRewards(req.getRewardType()).toItemProtoStream().toList();
        
        // Add rewards
        for (int i = 0; i < req.getTimes(); i++) {
            var reward = DailyInstanceReward.newInstance()
                    .setExp(data.getEnergyConsume());
            
            rewardList.forEach(reward::addSelect);
            
            rsp.addRewards(reward);
        }
        
        // Send response
        return session.encodeMsg(NetMsgId.daily_instance_raid_succeed_ack, rsp);
    }

}
