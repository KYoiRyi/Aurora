package emu.nebula.game.tower;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

public enum StarTowerRoomType {
    BattleRoom       (0),
    EliteBattleRoom  (1),
    BossRoom         (2),
    FinalBossRoom    (3),
    DangerRoom       (4),
    HorrorRoom       (5),
    ShopRoom         (6),
    EventRoom        (7),
    UnifyBattleRoom  (15);
    
    @Getter
    private final int value;
    private final static Int2ObjectMap<StarTowerRoomType> map = new Int2ObjectOpenHashMap<>();
    
    static {
        for (StarTowerRoomType type : StarTowerRoomType.values()) {
            map.put(type.getValue(), type);
        }
    }
    
    private StarTowerRoomType(int value) {
        this.value = value;
    }
    
    public static StarTowerRoomType getByValue(int value) {
        return map.get(value);
    }
}
