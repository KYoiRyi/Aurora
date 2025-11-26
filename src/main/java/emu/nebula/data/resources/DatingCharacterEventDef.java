package emu.nebula.data.resources;

import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import lombok.Getter;

@Getter
@ResourceType(name = "DatingCharacterEvent.json", loadPriority = LoadPriority.LOW)
public class DatingCharacterEventDef extends DatingLandmarkEventDef {
    
}
