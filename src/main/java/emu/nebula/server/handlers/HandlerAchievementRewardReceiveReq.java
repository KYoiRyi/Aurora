package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.AchievementRewardReceive.AchievementRewardReq;
import emu.nebula.net.HandlerId;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.achievement_reward_receive_req)
public class HandlerAchievementRewardReceiveReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = AchievementRewardReq.parseFrom(message);
        
        // Claim rewards
        var change = session.getPlayer().getAchievementManager().recvRewards(req.getIds());
        
        if (change == null) {
            return session.encodeMsg(NetMsgId.achievement_reward_receive_failed_ack);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.achievement_reward_receive_succeed_ack, change.toProto());
    }

}
