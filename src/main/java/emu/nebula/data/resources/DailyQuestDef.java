package emu.nebula.data.resources;

import java.util.Arrays;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import lombok.Getter;

@Getter
@ResourceType(name = "DailyQuest.json")
public class DailyQuestDef extends BaseDef {
    private int Id;
    private boolean Apear;
    private int Active;
    private int ItemTid;
    private int ItemQty;
    
    private int CompleteCond;
    private int CompleteCondClient;
    private String CompleteCondParams;
    
    private transient int[] condParams;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public int[] getCompleteCondParams() {
        return this.condParams;
    }
    
    @Override
    public void onLoad() {
        this.condParams = Arrays.stream(this.CompleteCondParams.split("[\\[,\\]]"))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
        
        this.CompleteCondParams = null;
    }
}
