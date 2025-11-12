package emu.nebula.data.resources;

import emu.nebula.data.BaseDef;
import emu.nebula.data.GameData;
import emu.nebula.data.ResourceType;
import emu.nebula.data.ResourceType.LoadPriority;
import lombok.Getter;

@Getter
@ResourceType(name = "Chat.json", loadPriority = LoadPriority.LOW)
public class ChatDef extends BaseDef {
    private int Id;
    private int AddressBookId;
    private int PreChatId;
    
    private int TriggerType;
    private int TriggerCond;
    private String TriggerCondParam;
    
    private int Reward1;
    private int RewardQty1;
    
    @Override
    public int getId() {
        return Id;
    }
    
    @Override
    public void onLoad() {
        var character = GameData.getCharacterDataTable().get(this.AddressBookId);
        
        if (character != null) {
            character.getChats().add(this);
        }
    }
}
