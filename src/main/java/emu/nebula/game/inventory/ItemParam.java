package emu.nebula.game.inventory;

import dev.morphia.annotations.Entity;
import emu.nebula.proto.Public.ItemTpl;
import lombok.Getter;

@Getter
@Entity(useDiscriminator = false)
public class ItemParam {
    public int id;
    public int count;
    
    @Deprecated // Morphia only
    public ItemParam() {
        
    }
    
    public ItemParam(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public ItemTpl toProto() {
        var proto = ItemTpl.newInstance()
                .setTid(this.getId())
                .setQty(this.getCount());
        
        return proto;
    }
}
