package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "WorldClass.json")
public class WorldClassDef extends BaseDef {
    private int Id;
    private int Exp;
    private String Reward;
    
    @Override
    public int getId() {
        return Id;
    }
}
