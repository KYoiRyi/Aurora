package emu.nebula.data.resources;

import emu.nebula.GameConstants;
import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import emu.nebula.game.inventory.ItemParamMap;

import lombok.Getter;

@Getter
@ResourceType(name = "DiscPromote.json", loadPriority = LoadPriority.LOW)
public class DiscPromoteDef extends BaseDef {
    private int Id;
    private int Group;
    private int AdvanceLvl;
    
    private int ItemId1;
    private int Num1;
    private int ItemId2;
    private int Num2;
    private int ItemId3;
    private int Num3;
    private int ExpenseGold;
    
    private transient ItemParamMap materials;
    
    @Override
    public int getId() {
        return Id;
    }

    @Override
    public void onLoad() {
        this.materials = new ItemParamMap();
        
        if (this.ItemId1 > 0 && this.Num1 > 0) {
            this.materials.add(this.ItemId1, this.Num1);
        }
        if (this.ItemId2 > 0 && this.Num2 > 0) {
            this.materials.add(this.ItemId2, this.Num2);
        }
        if (this.ItemId3 > 0 && this.Num3 > 0) {
            this.materials.add(this.ItemId3, this.Num3);
        }
        if (this.ExpenseGold > 0) {
            this.materials.add(GameConstants.GOLD_ITEM_ID, this.ExpenseGold);
        }
    }
}
