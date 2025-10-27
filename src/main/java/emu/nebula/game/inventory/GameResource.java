package emu.nebula.game.inventory;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import emu.nebula.Nebula;
import emu.nebula.database.GameDatabaseObject;
import emu.nebula.game.player.Player;
import emu.nebula.proto.Public.Res;
import emu.nebula.util.Utils;
import lombok.Getter;

@Getter
@Entity(value = "resources", useDiscriminator = false)
public class GameResource implements GameDatabaseObject {
    @Id 
    private ObjectId uid;
    @Indexed
    private int playerUid;
    
    public int resourceId;
    public int count;
    
    @Deprecated // Morphia only
    public GameResource() {
        
    }
    
    public GameResource(Player player, int id, int count) {
        this.playerUid = player.getUid();
        this.resourceId = id;
        this.count = count;
    }
    
    public int add(int amount) {
        int oldCount = this.count;
        this.count = Utils.safeAdd(this.count, amount, Integer.MAX_VALUE, 0);
        return this.count - oldCount;
    }
    
    @Override
    public void save() {
        if (this.getCount() <= 0) {
            if (this.getUid() != null) {
                Nebula.getGameDatabase().delete(this);
            }
        } else {
            Nebula.getGameDatabase().save(this);
        }
    }
    
    // Proto
    
    public Res toProto() {
        var proto = Res.newInstance()
                .setTid(this.getResourceId())
                .setQty(this.getCount());
        
        return proto;
    }
}
