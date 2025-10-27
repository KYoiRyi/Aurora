package emu.nebula.data.resources;

import com.google.gson.annotations.SerializedName;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "MallShop.json")
public class MallShopDef extends BaseDef {
    @SerializedName("Id")
    private String IdString;
    private int Stock;
    private int ItemId;
    private int CurrencyItemId;
    private int ItemQty;
    
    @Override
    public int getId() {
        return IdString.hashCode();
    }
}
