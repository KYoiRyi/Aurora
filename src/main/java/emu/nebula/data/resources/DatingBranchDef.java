package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import lombok.Getter;

@Getter
@ResourceType(name = "DatingBranch.json", loadPriority = LoadPriority.LOW)
public class DatingBranchDef extends BaseDef {
    private int Id;
    private int DatingEventType;
    private int[] DatingEventParams;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public int getLandmarkId() {
        if (this.DatingEventParams.length <= 0) {
            return 0;
        }
        
        return this.DatingEventParams[0];
    }
    
    @Override
    public void onLoad() {
        
    }
}
