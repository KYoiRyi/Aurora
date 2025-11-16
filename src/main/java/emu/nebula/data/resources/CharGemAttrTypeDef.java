package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import emu.nebula.util.WeightedList;
import lombok.Getter;

@Getter
@ResourceType(name = "CharGemAttrType.json")
public class CharGemAttrTypeDef extends BaseDef {
    private int Id;
    private int GroupId;
    
    private transient WeightedList<CharGemAttrValueDef> values;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public CharGemAttrValueDef getRandomValueData() {
        return this.getValues().next();
    }
    
    public int getRandomValue() {
        return this.getRandomValueData().getId();
    }
    
    @Override
    public void onLoad() {
        this.values = new WeightedList<>();
        
        var data = GameData.getCharGemAttrGroupDataTable().get(this.GroupId);
        if (data != null) {
            data.getAttributeTypes().add(this);
        }
    }
}
