package emu.nebula.data.resources;

import java.util.ArrayList;
import java.util.List;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.game.dating.DatingEvent;
import emu.nebula.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

@Getter
@ResourceType(name = "DatingLandmark.json")
public class DatingLandmarkDef extends BaseDef {
    private int Id;
    
    private transient List<DatingEvent> afterBranches;
    private transient Object2ObjectMap<String, List<DatingEvent>> characterEvents;
    private transient Object2ObjectMap<String, List<DatingEvent>> landmarkEvents;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public int getRandomAfterBranchId() {
        var event = Utils.randomElement(this.afterBranches);
        
        if (event == null) {
            return 0;
        }
        
        return event.getId();
    }
    
    public int getRandomCharacterEventId() {
        var list = new ArrayList<DatingEvent>();
        
        for (var events : this.characterEvents.values()) {
            list.addAll(events);
        }
        
        // Get random event
        var event = Utils.randomElement(list);
        
        if (event == null) {
            return 0;
        }
        
        return event.getId();
    }

    @Override
    public void onLoad() {
        this.afterBranches = new ArrayList<>();
        this.characterEvents = new Object2ObjectOpenHashMap<>();
        this.landmarkEvents = new Object2ObjectOpenHashMap<>();
    }
}
