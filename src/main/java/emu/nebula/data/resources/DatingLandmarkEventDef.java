package emu.nebula.data.resources;

import java.util.ArrayList;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import emu.nebula.game.dating.DatingEvent;
import lombok.Getter;

@Getter
@ResourceType(name = "DatingLandmarkEvent.json", loadPriority = LoadPriority.LOW)
public class DatingLandmarkEventDef extends BaseDef implements DatingEvent {
    private int Id;
    private int DatingEventType;
    private int Affinity;
    private int[] DatingEventParams;
    private String Response;
    
    private transient emu.nebula.game.dating.DatingEventType type;
    
    @Override
    public int getId() {
        return Id;
    }
    
    public int getLandmarkId() {
        if (this.DatingEventParams.length <= 0) {
            return 0;
        }
        
        return this.DatingEventParams[0];
    }
    
    @Override
    public void onLoad() {
        // Cache dating event type
        this.type = emu.nebula.game.dating.DatingEventType.getByValue(this.getDatingEventType());
        
        // Add to landmark data
        var data = GameData.getDatingLandmarkDataTable().get(this.getLandmarkId());
        if (data == null) {
            return;
        }
        
        switch (this.getType()) {
            case Landmark -> {
                data.getLandmarkEvents()
                    .computeIfAbsent(this.getResponse(), s -> new ArrayList<>())
                    .add(this);
            }
            case Regular -> {
                data.getCharacterEvents()
                    .computeIfAbsent(this.getResponse(), s -> new ArrayList<>())
                    .add(this);
            }
            case AfterBranch -> {
                data.getAfterBranches().add(this);
            }
            default -> {
                // Ignored
            }
        }
    }
}
