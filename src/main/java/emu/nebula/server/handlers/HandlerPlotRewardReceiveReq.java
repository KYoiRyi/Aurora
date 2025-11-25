package emu.nebula.server.handlers;

import emu.nebula.net.NetHandler;
import emu.nebula.net.NetMsgId;
import emu.nebula.proto.Public.ChangeInfo;
import emu.nebula.proto.Public.UI32;
import emu.nebula.net.HandlerId;
import emu.nebula.data.GameData;
import emu.nebula.net.GameSession;

@HandlerId(NetMsgId.plot_reward_receive_req)
public class HandlerPlotRewardReceiveReq extends NetHandler {

    @Override
    public byte[] handle(GameSession session, byte[] message) throws Exception {
        // Parse request
        var req = UI32.parseFrom(message);
        
        // Get plot data
        var plot = GameData.getPlotDataTable().get(req.getValue());
        
        if (plot == null) {
            // Send empty packet to prevent the client from crashing
            return session.encodeMsg(NetMsgId.plot_reward_receive_succeed_ack, ChangeInfo.newInstance());
        }
        
        // Get character
        var character = session.getPlayer().getCharacters().getCharacterById(plot.getChar());
        
        if (character == null) {
            // Send empty packet to prevent the client from crashing
            return session.encodeMsg(NetMsgId.plot_reward_receive_succeed_ack, ChangeInfo.newInstance());
        }
        
        // Complete plot
        var change = character.recvPlotReward(plot.getId());
        
        if (change == null) {
            // Should never happen
            // Send empty packet to prevent the client from crashing
            return session.encodeMsg(NetMsgId.plot_reward_receive_succeed_ack, ChangeInfo.newInstance());
        }
        
        // Encode and send
        return session.encodeMsg(NetMsgId.plot_reward_receive_succeed_ack, change.toProto());
    }

}
