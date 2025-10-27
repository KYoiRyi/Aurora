package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "StarTower.json")
public class StarTowerDef extends BaseDef {
    private int Id;
    private int[] FloorNum;
    
    @Override
    public int getId() {
        return Id;
    }
}
