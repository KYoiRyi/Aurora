package emu.nebula.game.character;

import dev.morphia.annotations.Entity;
import emu.nebula.data.resources.ChatDef;
import emu.nebula.proto.Public.Chat;
import lombok.Getter;
import us.hebi.quickbuf.RepeatedInt;

@Getter
@Entity(useDiscriminator = false)
public class CharacterChat {
    private int id;
    private int process;
    private boolean end;
    
    private int[] options;
    
    @Deprecated // Morphia only
    public CharacterChat() {
        
    }
    
    public CharacterChat(ChatDef data) {
        this.id = data.getId();
    }
    
    // Proto
    
    public Chat toProto() {
        var proto = Chat.newInstance()
                .setId(this.getId())
                .setProcess(this.getProcess());
        
        if (this.getOptions() != null) {
            proto.addAllOptions(this.getOptions());
        }
        
        return proto;
    }

    public void report(int process, RepeatedInt options, boolean end) {
        this.process = process;
        this.end = end;
        
        if (options.length() > 0 && options.length() <= 5) {
            this.options = options.toArray();
        }
    }
}
