package emu.nebula.game.tower;

import emu.nebula.proto.PublicStarTower.StarTowerRoomCase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StarTowerCase {
    private int id;
    
    @Setter(AccessLevel.NONE)
    private CaseType type;
    
    // Extra data
    private int teamLevel;
    private int subNoteSkillNum;

    private int floorId;
    private int roomType;
    
    // Selector
    private IntList ids;
    
    public StarTowerCase(CaseType type) {
        this.type = type;
    }
    
    public void addId(int id) {
        if (this.ids == null) {
            this.ids = new IntArrayList();
        }
        
        this.ids.add(id);
    }

    public int selectId(int index) {
        if (this.getIds() == null) {
            return 0;
        }
        
        if (index < 0 || index >= this.getIds().size()) {
            return 0;
        }
        
        return this.getIds().getInt(index);
    }
    
    public StarTowerRoomCase toProto() {
        var proto = StarTowerRoomCase.newInstance()
                .setId(this.getId());
        
        switch (this.type) {
            case Battle -> {
                proto.getMutableBattleCase()
                    .setSubNoteSkillNum(this.getSubNoteSkillNum());
            }
            case OpenDoor -> {
                proto.getMutableDoorCase()
                    .setFloor(this.getFloorId())
                    .setType(this.getRoomType());
            }
            case SyncHP, RecoveryHP -> {
                proto.getMutableSyncHPCase();
            }
            case SelectSpecialPotential -> {
                proto.getMutableSelectSpecialPotentialCase()
                    .setTeamLevel(this.getTeamLevel())
                    .addAllIds(this.getIds().toIntArray());
            }
            case PotentialSelect -> {
                proto.getMutableSelectPotentialCase();
            }
            default -> {
                
            }
        }
        
        return proto;
    }
}
