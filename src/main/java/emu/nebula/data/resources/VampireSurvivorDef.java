package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "VampireSurvivor.json")
public class VampireSurvivorDef extends BaseDef {
    private int Id;
    private int Mode;
    private int NeedWorldClass;
    private int[] FateCardBundle;
    
    private int NormalScore1;
    private int EliteScore1;
    private int BossScore1;
    private int TimeScore1;
    private int TimeLimit1;
    
    private int NormalScore2;
    private int EliteScore2;
    private int BossScore2;
    private int TimeScore2;
    private int TimeLimit2;
    
    @Override
    public int getId() {
        return Id;
    }
}
