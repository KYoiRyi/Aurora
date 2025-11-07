package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.game.inventory.ItemParamMap;
import lombok.Getter;

@Getter
@ResourceType(name = "DailyQuestActive.json")
public class DailyQuestActiveDef extends BaseDef {
    private int Id;
    private int Active;
    
    private int ItemTid1;
    private int Number1;
    private int ItemTid2;
    private int Number2;
    
    private transient ItemParamMap rewards;
    
    @Override
    public int getId() {
        return Id;
    }
    
    @Override
    public void onLoad() {
        this.rewards = new ItemParamMap();
        
        if (this.ItemTid1 > 0 && this.Number1 > 0) {
            this.rewards.add(this.ItemTid1, this.Number1);
        }
        if (this.ItemTid2 > 0 && this.Number2 > 0) {
            this.rewards.add(this.ItemTid2, this.Number2);
        }
    }
}
