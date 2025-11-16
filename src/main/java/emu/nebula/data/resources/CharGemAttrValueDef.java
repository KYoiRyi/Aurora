package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import lombok.Getter;

@Getter
@ResourceType(name = "CharGemAttrValue.json", loadPriority = LoadPriority.LOW)
public class CharGemAttrValueDef extends BaseDef {
    private int Id;
    private int TypeId;
    private int AttrType;
    private int AttrTypeFirstSubtype;
    private int AttrTypeSecondSubtype;
    private int Rarity;
    
    @Override
    public int getId() {
        return Id;
    }
    
    @Override
    public void onLoad() {
        var data = GameData.getCharGemAttrTypeDataTable().get(this.TypeId);
        if (data != null) {
            data.getValues().add(this.getRarity(), this);
        }
    }
}
