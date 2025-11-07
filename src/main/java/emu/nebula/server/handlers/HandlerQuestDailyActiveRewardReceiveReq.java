package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.QuestDailyActiveRewardRecevie.QuestDailyActiveRewardReceiveResp;
import it.unimi.dsi.fastutil.ints.IntList;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.quest_daily_active_reward_receive_req)
public class HandlerQuestDailyActiveRewardReceiveReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Receive rewards
        var change = session.getPlayer().getQuestManager().claimActiveRewards();
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.quest_daily_active_reward_receive_failed_ack);
        }
        
        // Build response
        var rsp = QuestDailyActiveRewardReceiveResp.newInstance()
                .setChange(change.toProto());
        
        if (change.getExtraData() != null && change.getExtraData() instanceof IntList ids) {
            ids.forEach(rsp::addActiveIds);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.quest_daily_active_reward_receive_succeed_ack, rsp);
    }

}
