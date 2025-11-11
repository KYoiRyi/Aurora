package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.InfinityTowerInfo.InfinityTowerInfoResp;
import emu.nebula.proto.Public.InfinityTowerLevelInfo;
import emu.nebula.net.HandlerId;
import emu.nebula.Nebula;
import emu.nebula.data.GameData;
import emu.nebula.net.GameSession;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

@HandlerId(NetMsgId.infinity_tower_info_req)
public class HandlerInfinityTowerInfoReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Build response
        var rsp = InfinityTowerInfoResp.newInstance()
                .setBountyLevel(0);
        
        // Add unlocked levels
        if (Nebula.getConfig().getServerOptions().unlockInstances) {
            // Force unlock every level
            var levels = new Int2ObjectOpenHashMap<InfinityTowerLevelInfo>();       
            
            for (var data : GameData.getInfinityTowerLevelDataTable()) {
                int id = (int) Math.floor(data.getId() / 10000D);
                
                var info = levels.computeIfAbsent(
                    id,
                    diff -> InfinityTowerLevelInfo.newInstance().setId(id)
                );
                
                if (data.getId() > info.getLevelId()) {
                    info.setLevelId(data.getId());
                }
            }
            
            for (var info : levels.values()) {
                rsp.addInfos(info);
            }
        } else {
            // Get infinite arena log from player progress
            for (var entry : session.getPlayer().getProgress().getInfinityArenaLog().int2IntEntrySet()) {
                var info = InfinityTowerLevelInfo.newInstance()
                        .setId(entry.getIntKey())
                        .setLevelId(entry.getIntValue());
                
                rsp.addInfos(info);
            }
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.infinity_tower_info_succeed_ack, rsp);
    }

}
