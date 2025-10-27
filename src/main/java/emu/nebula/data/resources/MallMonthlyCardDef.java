package emu.nebula.data.resources;

import com.google.gson.annotations.SerializedName;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "MallMonthlyCard.json")
public class MallMonthlyCardDef extends BaseDef {
    @SerializedName("Id")
    private String IdString;
    private int MonthlyCardId;
    private int Price;
    private int BaseItemId;
    private int BaseItemQty;
    
    @Override
    public int getId() {
        return IdString.hashCode();
    }
}
