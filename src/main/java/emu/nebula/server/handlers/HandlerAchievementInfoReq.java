package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.Public.Achievement;
import emu.nebula.proto.Public.Achievements;
import emu.nebula.proto.Public.QuestProgress;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.achievement_info_req)
public class HandlerAchievementInfoReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Build response
        var rsp = Achievements.newInstance();
        
        for (var data : GameData.getAchievementDataTable()) {
            var progress = QuestProgress.newInstance()
                    .setCur(data.getAimNumShow())
                    .setMax(data.getAimNumShow());
            
            var info = Achievement.newInstance()
                    .setId(data.getId())
                    .setStatus(2)
                    .setCompleted(session.getPlayer().getCreateTime())
                    .addProgress(progress);
            
            rsp.addList(info);
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.achievement_info_succeed_ack, rsp);
    }

}
