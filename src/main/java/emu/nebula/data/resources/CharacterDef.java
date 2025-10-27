package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "Character.json")
public class CharacterDef extends BaseDef {
    private int Id;
    private String Name;
    
    private boolean Available;
    private int Grade;
    private int DefaultSkinId;
    private int AdvanceSkinId;
    private int AdvanceGroup;
    private int[] SkillsUpgradeGroup;
    
    @Override
    public int getId() {
        return Id;
    }

    public int getSkillsUpgradeGroup(int index) {
        if (index < 0 || index >= this.SkillsUpgradeGroup.length) {
            return -1;
        }
        
        return this.SkillsUpgradeGroup[index];
    }
}
