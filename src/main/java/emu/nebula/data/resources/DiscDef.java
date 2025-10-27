package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "Disc.json")
public class DiscDef extends BaseDef {
    private int Id;
    private int StrengthenGroupId;
    private int PromoteGroupId;
    private int TransformItemId;
    
    @Override
    public int getId() {
        return Id;
    }

    @Override
    public void onLoad() {
        
    }
}
