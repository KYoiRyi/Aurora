package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "GuideGroup.json")
public class GuideGroupDef extends BaseDef {
    private int Id;
    private boolean IsActive;
    
    @Override
    public int getId() {
        return Id;
    }
}
