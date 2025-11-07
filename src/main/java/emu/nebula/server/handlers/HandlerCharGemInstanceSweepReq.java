package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.CharGemInstanceSweep.CharGemInstanceSweepReq;
import emu.nebula.proto.CharGemInstanceSweep.CharGemInstanceSweepResp;
import emu.nebula.proto.CharGemInstanceSweep.CharGemInstanceSweepReward;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.game.quest.QuestCondType;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.char_gem_instance_sweep_req)
public class HandlerCharGemInstanceSweepReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = CharGemInstanceSweepReq.parseFrom(message);
        
        // Get instance data
        var data = GameData.getCharGemInstanceDataTable().get(req.getId());
        if (data == null) {
            return session.encodeMsg(NetMsgId.char_gem_instance_sweep_failed_ack);
        }
        
        // Sweep
        var change = session.getPlayer().getInstanceManager().sweepInstance(
                data,
                QuestCondType.CharGemInstanceClearTotal,
                session.getPlayer().getInstanceManager().getCharGemLog(),
                0,
                req.getTimes()
        );
        
        // Sanity check
        if (change == null) {
            return session.encodeMsg(NetMsgId.char_gem_instance_sweep_failed_ack);
        }
        
        // Build response
        var rsp = CharGemInstanceSweepResp.newInstance()
                .setChange(change.toProto());
        
        // Cache reward list
        var rewardList = data.getRewards().toItemTemplateStream().toList();
        
        // Add rewards
        for (int i = 0; i < req.getTimes(); i++) {
            var reward = CharGemInstanceSweepReward.newInstance()
                    .setExp(data.getEnergyConsume());
            
            rewardList.forEach(reward::addAwardItems);
            
            rsp.addRewards(reward);
        }
        
        // Send response
        return session.encodeMsg(NetMsgId.char_gem_instance_sweep_succeed_ack, rsp);
    }

}
