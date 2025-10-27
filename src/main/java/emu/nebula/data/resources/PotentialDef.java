package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "Potential.json")
public class PotentialDef extends BaseDef {
    private int Id;
    private int CharId;
    private int Build;
    private int MaxLevel;
    
    @Override
    public int getId() {
        return Id;
    }
}
