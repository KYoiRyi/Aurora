package emu.nebula.game.character;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

@Getter
public enum ElementType {
    INHERIT     (0),
    AQUA        (1, 90018),
    FIRE        (2, 90019),
    EARTH       (3, 90021),
    WIND        (4, 90020),
    LIGHT       (5, 90022),
    DARK        (6, 90023),
    NONE        (7);
    
    private final int value;
    private final int subNoteSkillItemId;
    private final static Int2ObjectMap<ElementType> map = new Int2ObjectOpenHashMap<>();
    
    static {
        for (ElementType type : ElementType.values()) {
            map.put(type.getValue(), type);
        }
    }
    
    private ElementType(int value) {
        this(value, 0);
    }
    
    private ElementType(int value, int subNoteSkillItemId) {
        this.value = value;
        this.subNoteSkillItemId = subNoteSkillItemId;
    }
    
    public static ElementType getByValue(int value) {
        return map.get(value);
    }
}
