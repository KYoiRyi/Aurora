package emu.nebula.game.player;

import java.util.ArrayList;
import java.util.List;

import emu.nebula.GameConstants;
import emu.nebula.proto.AnyOuterClass.Any;
import emu.nebula.proto.Public.ChangeInfo;
import lombok.Getter;
import lombok.Setter;
import us.hebi.quickbuf.ProtoMessage;

@Getter
public class PlayerChangeInfo {
    private boolean success;
    private List<Any> list;
    
    @Setter
    private Object extraData;
    
    public PlayerChangeInfo() {
        this.list = new ArrayList<>();
    }
    
    public PlayerChangeInfo setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public void add(ProtoMessage<?> proto) {
        var any = Any.newInstance()
                .setTypeUrl(GameConstants.PROTO_BASE_TYPE_URL + proto.getClass().getSimpleName())
                .setValue(proto.toByteArray());
        
        this.list.add(any);
    }
    
    // Proto
    
    public ChangeInfo toProto() {
        var proto = ChangeInfo.newInstance();
        
        for (var any : this.getList()) {
            proto.addProps(any);
        }
        
        return proto;
    }
}
