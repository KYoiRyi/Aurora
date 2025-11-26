package emu.nebula.game.dating;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

public enum DatingEventType {
    Start           (1),
    End             (2),
    Landmark        (3),
    Regular         (4),
    LimitedLandmark (5),
    BranchA         (6),
    BranchB         (7),
    BeforeBranch    (8),
    AfterBranch     (9);

    @Getter
    private final int value;
    private final static Int2ObjectMap<DatingEventType> map = new Int2ObjectOpenHashMap<>();

    static {
        for (DatingEventType type : DatingEventType.values()) {
            map.put(type.getValue(), type);
        }
    }

    private DatingEventType(int value) {
        this.value = value;
    }

    public static DatingEventType getByValue(int value) {
        return map.get(value);
    }
}
