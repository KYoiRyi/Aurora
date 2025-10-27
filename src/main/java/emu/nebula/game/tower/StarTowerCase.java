package emu.nebula.game.tower;

import emu.nebula.proto.PublicStarTower.StarTowerRoomCase;
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
    
    private int floorId;
    
    // Select
    private int[] ids;
    
    public StarTowerCase(CaseType type) {
        this.type = type;
    }
    
    public StarTowerRoomCase toProto() {
        var proto = StarTowerRoomCase.newInstance()
                .setId(this.getId());
        
        switch (this.type) {
            case Battle -> {
                proto.getMutableBattleCase();
            }
            case OpenDoor -> {
                proto.getMutableDoorCase();
            }
            case SyncHP -> {
                proto.getMutableSyncHPCase();
            }
            case SelectSpecialPotential -> {
                proto.getMutableSelectSpecialPotentialCase();
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
