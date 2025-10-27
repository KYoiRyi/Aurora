package emu.nebula.game.tower;

import dev.morphia.annotations.Entity;
import emu.nebula.proto.PublicStarTower.BuildPotential;
import emu.nebula.proto.PublicStarTower.StarTowerBuildInfo;
import emu.nebula.proto.PublicStarTower.TowerBuildChar;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.Getter;

@Getter
@Entity(useDiscriminator = false)
public class StarTowerBuild {
    private int id;
    private String name;
    private boolean lock;
    private boolean preference;
    private int score;
    private int[] charIds;
    private int[] discIds;
    
    private Int2IntMap potentials;
    
    @Deprecated
    public StarTowerBuild() {
        
    }
    
    public StarTowerBuild(StarTowerInstance instance) {
        this.name = "";
        this.potentials = new Int2IntOpenHashMap();
        
        this.charIds = instance.getChars().stream()
                .filter(c -> c.getId() > 0)
                .mapToInt(c -> c.getId())
                .toArray();
        this.discIds = instance.getDiscs().stream()
                .filter(d -> d.getId() > 0)
                .mapToInt(d -> d.getId())
                .toArray();
        
        // Add potentials
        for (int id : instance.getPotentials()) {
            this.getPotentials().put(id, instance.getItemCount(id));
        }
    }
    
    public void setId(StarTowerManager manager) {
        this.id = manager.getNextBuildId();
    }
    
    // Proto
    
    public StarTowerBuildInfo toProto() {
        var proto = StarTowerBuildInfo.newInstance();
        
        // Basic data
        proto.getMutableBrief()
            .setId(this.getId())
            .setName(this.getName())
            .setLock(this.isLock())
            .setPreference(this.isPreference())
            .setScore(this.getScore())
            .addAllDiscIds(this.getDiscIds());
        
        // Add characters
        for (int id : charIds) {
            var charProto = TowerBuildChar.newInstance()
                    .setCharId(id);
            
            proto.getMutableBrief().addChars(charProto);
        }
        
        // Build detail
        var detail = proto.getMutableDetail();
        
        for (var entry : this.getPotentials().int2IntEntrySet()) {
            var potential = BuildPotential.newInstance()
                    .setPotentialId(entry.getIntKey())
                    .setLevel(entry.getIntValue());
            
            detail.getMutablePotentials().add(potential);
        }
        
        return proto;
    }
}
