package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "CharacterUpgrade.json")
public class CharacterUpgradeDef extends BaseDef {
    private int Level;
    private int Exp;
    
    @Override
    public int getId() {
        return Level;
    }
}
