package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "DiscPromoteLimit.json")
public class DiscPromoteLimitDef extends BaseDef {
    private int Id;
    private int Rarity;
    private int Phase;
    private int MaxLevel;
    private int WorldClassLimit;
    
    @Override
    public int getId() {
        return Id;
    }
}
