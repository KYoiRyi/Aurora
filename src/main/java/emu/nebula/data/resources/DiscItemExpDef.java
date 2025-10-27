package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "DiscItemExp.json")
public class DiscItemExpDef extends BaseDef {
    private int ItemId;
    private int Exp;
    
    @Override
    public int getId() {
        return ItemId;
    }
}
