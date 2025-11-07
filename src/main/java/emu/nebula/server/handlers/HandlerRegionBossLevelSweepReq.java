package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.RegionBossLevelSweep.RegionBossLevelSweepReq;
import emu.nebula.proto.RegionBossLevelSweep.RegionBossLevelSweepResp;
import emu.nebula.proto.RegionBossLevelSweep.RegionBossLevelSweepRewards;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.game.quest.QuestCondType;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.region_boss_level_sweep_req)
public class HandlerRegionBossLevelSweepReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = RegionBossLevelSweepReq.parseFrom(message);
        
        // Get instance data
        var data = GameData.getRegionBossLevelDataTable().get(req.getId());
        if (data == null) {
            return session.encodeMsg(NetMsgId.region_boss_level_sweep_failed_ack);
        }
        
        // Sweep
        var change = session.getPlayer().getInstanceManager().sweepInstance(
                data,
                QuestCondType.RegionBossClearTotal,
                session.getPlayer().getInstanceManager().getRegionBossLog(),
                0,
                req.getTimes()
        );
        
        // Sanity check
        if (change == null) {
            return session.encodeMsg(NetMsgId.region_boss_level_sweep_failed_ack);
        }
        
        // Build response
        var rsp = RegionBossLevelSweepResp.newInstance()
                .setChange(change.toProto());
        
        // Cache reward list
        var rewardList = data.getRewards().toItemTemplateStream().toList();
        
        // Add rewards
        for (int i = 0; i < req.getTimes(); i++) {
            var reward = RegionBossLevelSweepRewards.newInstance()
                    .setExp(data.getEnergyConsume());
            
            rewardList.forEach(reward::addAwardItems);
            
            rsp.addRewards(reward);
        }
        
        // Send response
        return session.encodeMsg(NetMsgId.region_boss_level_sweep_succeed_ack, rsp);
    }

}
