package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.game.inventory.ItemParamMap;
import lombok.Getter;

@Getter
@ResourceType(name = "Plot.json")
public class PlotDef extends BaseDef {
    private int Id;
    private int Char;
    private int UnlockAffinityLevel;
    private String Rewards;
    
    private transient ItemParamMap rewards;
    
    @Override
    public int getId() {
        return this.Id;
    }
    
    public ItemParamMap getRewards() {
        return this.rewards;
    }
    
    @Override
    public void onLoad() {
        this.rewards = ItemParamMap.fromJsonString(this.Rewards);
    }
}
