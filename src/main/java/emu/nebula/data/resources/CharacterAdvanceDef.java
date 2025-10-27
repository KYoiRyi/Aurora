package emu.nebula.data.resources;

import emu.nebula.GameConstants;
import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import emu.nebula.game.inventory.ItemParamMap;

import lombok.Getter;

@Getter
@ResourceType(name = "CharacterAdvance.json", loadPriority = LoadPriority.LOW)
public class CharacterAdvanceDef extends BaseDef {
    private int Id;
    private int Group;
    private int AdvanceLvl;
    
    private int Tid1;
    private int Qty1;
    private int Tid2;
    private int Qty2;
    private int Tid3;
    private int Qty3;
    private int Tid4;
    private int Qty4;
    private int GoldQty;
    
    private transient ItemParamMap materials;
    
    @Override
    public int getId() {
        return Id;
    }

    @Override
    public void onLoad() {
        this.materials = new ItemParamMap();
        
        if (this.Tid1 > 0) {
            this.materials.add(this.Tid1, this.Qty1);
        }
        if (this.Tid2 > 0) {
            this.materials.add(this.Tid2, this.Qty2);
        }
        if (this.Tid3 > 0) {
            this.materials.add(this.Tid3, this.Qty3);
        }
        if (this.Tid4 > 0) {
            this.materials.add(this.Tid4, this.Qty4);
        }
        if (this.GoldQty > 0) {
            this.materials.add(GameConstants.GOLD_ITEM_ID, this.GoldQty);
        }
    }
}
