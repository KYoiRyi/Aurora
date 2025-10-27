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

    public int getMaxFloor(int stage) {
        int index = stage - 1;
        
        if (index < 0 || index >= this.FloorNum.length) {
            return 0;
        }
        
        return this.FloorNum[index];
    }
}
