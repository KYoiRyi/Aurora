package emu.nebula.game.tower;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

public enum CaseType {
    Battle                  (1),
    OpenDoor                (2),
    PotentialSelect         (3),
    FateCardSelect          (4),
    NoteSelect              (5),
    NpcEvent                (6),
    SelectSpecialPotential  (7),
    RecoveryHP              (8),
    NpcRecoveryHP           (9),
    Hawker                  (10),
    StrengthenMachine       (11),
    DoorDanger              (12),
    SyncHP                  (13);
    
    @Getter
    private final int value;
    private final static Int2ObjectMap<CaseType> map = new Int2ObjectOpenHashMap<>();
    
    static {
        for (CaseType type : CaseType.values()) {
            map.put(type.getValue(), type);
        }
    }
    
    private CaseType(int value) {
        this.value = value;
    }
    
    public static CaseType getByValue(int value) {
        return map.get(value);
    }
}
