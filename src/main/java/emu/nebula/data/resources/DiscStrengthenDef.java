package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;

import lombok.Getter;

@Getter
@ResourceType(name = "DiscStrengthen.json")
public class DiscStrengthenDef extends BaseDef {
    private int Id;
    private int Exp;
    
    @Override
    public int getId() {
        return Id;
    }
}
