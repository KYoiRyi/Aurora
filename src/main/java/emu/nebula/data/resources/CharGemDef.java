package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "CharGem.json")
public class CharGemDef extends BaseDef {
    private int Id;
    private int GenerateCostTid;
    private int RefreshCostTid;
    private int Type;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public CharGemSlotControlDef getControlData() {
        return GameData.getCharGemSlotControlDataTable().get(this.Type);
    }
}
