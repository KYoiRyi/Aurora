package emu.nebula.net;

import lombok.SneakyThrows;
import us.hebi.quickbuf.ProtoMessage;
import us.hebi.quickbuf.ProtoSink;

public abstract class NetHandler {

    public boolean requireSession() {
        return true;
    }
    
    public boolean requirePlayer() {
        return true;
    }
    
    public byte[] encodeMsg(int msgId, byte[] packet) {
        // Create data array
        byte[] data = new byte[packet.length + 2];
        
        // Encode msgId
        short id = (short) msgId;
        data[0] = (byte) (id >> 8);
        data[1] = (byte) id;
        
        // Copy packet to data array
        System.arraycopy(packet, 0, data, 2, packet.length);
        
        // Complete
        return data;
    }

    @SneakyThrows
    public byte[] encodeMsg(int msgId, ProtoMessage<?> proto) {
        // Create data array
        byte[] data = new byte[proto.getCachedSize() + 2];
        
        // Encode msgId
        short id = (short) msgId;
        data[0] = (byte) (id >> 8);
        data[1] = (byte) id;
        
        // Create proto sink
        var output = ProtoSink.newInstance(data, 2, proto.getCachedSize());
        
        // Copy packet to data array
        proto.writeTo(output);
        
        // Complete
        return data;
    }
    
    public byte[] encodeMsg(int msgId) {
        // Create data array
        byte[] data = new byte[2];
        
        // Encode msgId
        short id = (short) msgId;
        data[0] = (byte) (id >> 8);
        data[1] = (byte) id;
        
        return data;
    }

    public abstract byte[] handle(GameSession session, byte[] message) throws Exception;

}
