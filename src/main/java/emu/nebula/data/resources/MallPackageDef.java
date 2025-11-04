package emu.nebula.data.resources;

import com.google.gson.annotations.SerializedName;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.game.inventory.ItemParamMap;
import lombok.Getter;

@Getter
@ResourceType(name = "MallPackage.json")
public class MallPackageDef extends BaseDef {
    @SerializedName("Id")
    private String IdString;
    private int Stock;
    private int CurrencyType;
    private int CurrencyItemId;
    private int CurrencyItemQty;
    private String Items;
    
    private transient ItemParamMap products;
    
    @Override
    public int getId() {
        return IdString.hashCode();
    }
    
    @Override
    public void onLoad() {
        this.products = ItemParamMap.fromJsonString(this.Items);
    }
}
