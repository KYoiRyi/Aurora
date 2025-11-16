package emu.nebula.data.resources;

import java.util.List;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import emu.nebula.util.JsonUtils;
import emu.nebula.util.WeightedList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

@Getter
@ResourceType(name = "CharGemAttrGroup.json", loadPriority = LoadPriority.HIGH)
public class CharGemAttrGroupDef extends BaseDef {
    private int GroupId;
    private int GroupType;
    private int Weight;
    private String UniqueAttrNumWeight;
    
    private transient WeightedList<Integer> uniqueAttrNum;
    private transient List<CharGemAttrTypeDef> attributeTypes;
    
    @Override
    public int getId() {
        return GroupId;
    }
    
    public int getRandomUniqueAttrNum() {
        if (this.uniqueAttrNum == null) {
            return 0;
        }
        
        return this.uniqueAttrNum.next();
    }
    
    public CharGemAttrTypeDef getRandomAttributeType(IntList list) {
        // Setup blacklist to prevent the same attribute from showing up twice
        var blacklist = new IntOpenHashSet();
        
        for (int id : list) {
            var value = GameData.getCharGemAttrValueDataTable().get(id);
            if (value == null) continue;
            
            int blacklistId = value.getTypeId();
            blacklist.add(blacklistId);
        }
        
        // Create random generator
        var random = new WeightedList<CharGemAttrTypeDef>();
        
        for (var type : this.getAttributeTypes()) {
            if (blacklist.contains(type.getId())) {
                continue;
            }
            
            random.add(100, type);
        }
        
        return random.next();
    }
    
    @Override
    public void onLoad() {
        this.uniqueAttrNum = new WeightedList<>();
        this.attributeTypes = new ObjectArrayList<>();
        
        if (this.UniqueAttrNumWeight != null) {
            var json = JsonUtils.decodeMap(this.UniqueAttrNumWeight, Integer.class, Integer.class);
            
            for (var entry : json.entrySet()) {
                this.uniqueAttrNum.add(entry.getValue(), entry.getKey());
            }
        }
    }
}
