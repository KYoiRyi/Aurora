package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.ScoreBossRank.ScoreBossRankInfo;
import emu.nebula.net.HandlerId;
import emu.nebula.Nebula;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.score_boss_rank_req)
public class HandlerScoreBossRankReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Build response
        var rsp = ScoreBossRankInfo.newInstance()
                .setLastRefreshTime(Nebula.getCurrentTime());
        
        // Get self
        var self = session.getPlayer().getScoreBossManager().getRanking();
        
        if (self != null) {
            rsp.setSelf(self.toProto());
        }
        
        // Get ranking
        var ranking = Nebula.getGameContext().getScoreBossModule().getRanking();
        
        for (var entry : ranking) {
            // Check self
            if (self != null && self.getPlayerUid() == entry.getId()) {
                rsp.getMutableSelf().setRank(entry.getRank());
            }
            
            // Add to ranking
            rsp.addRank(entry);
        }
        
        // Set total
        rsp.setTotal(ranking.size());
        
        // Encode and send
        return session.encodeMsg(NetMsgId.score_boss_rank_succeed_ack, rsp);
    }

}
