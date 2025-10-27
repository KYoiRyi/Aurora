package emu.nebula.game.tower;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import emu.nebula.data.GameData;
import emu.nebula.database.GameDatabaseObject;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerManager;
import emu.nebula.proto.StarTowerApply.StarTowerApplyReq;
import lombok.Getter;

@Getter
@Entity(value = "star_tower", useDiscriminator = false)
public class StarTowerManager extends PlayerManager implements GameDatabaseObject {
    @Id
    private int uid;
    
    private transient StarTowerInstance instance;
    
    @Deprecated // Morphia only
    public StarTowerManager() {
        
    }
    
    public StarTowerManager(Player player) {
        super(player);
        this.uid = player.getUid();
        
        this.save();
    }
    
    public StarTowerInstance apply(StarTowerApplyReq req) {
        // Sanity checks
        var data = GameData.getStarTowerDataTable().get(req.getId());
        if (data == null) {
            return null;
        }
        
        // Get formation
        var formation = getPlayer().getFormations().getFormationById(req.getFormationId());
        if (formation == null) {
            return null;
        }
        
        // Create instance
        this.instance = new StarTowerInstance(this, data, formation, req);
        
        // Success
        return this.instance;
    }
}
