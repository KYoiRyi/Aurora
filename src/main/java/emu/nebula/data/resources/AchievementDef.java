package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;

import lombok.Getter;

@Getter
@ResourceType(name = "Achievement.json")
public class AchievementDef extends BaseDef {
    private int Id;
    private int Type;
    private int CompleteCond;
    private int AimNumShow;
    private int[] Prerequisites;
    
    // Reward
    private int Tid1;
    private int Qty1;
    
    @Override
    public int getId() {
        return Id;
    }

}
