package emu.nebula.game.character;

import java.util.ArrayList;
import java.util.List;

import dev.morphia.annotations.Entity;
import emu.nebula.GameConstants;
import emu.nebula.proto.Public.CharGemSlot;
import lombok.Getter;

@Getter
@Entity(useDiscriminator = false)
public class CharacterGemSlot {
    private int id;
    private List<CharacterGem> gems;
    
    @Deprecated // Morphia only
    public CharacterGemSlot() {
        
    }
    
    public CharacterGemSlot(int id) {
        this.id = id;
        this.gems = new ArrayList<>();
    }

    public CharacterGem getGem(int gemId) {
        if (gemId < 0 || gemId >= this.getGems().size()) {
            return null;
        }
        
        return this.getGems().get(gemId);
    }
    
    public boolean isFull() {
        return getGems().size() >= GameConstants.CHARACTER_MAX_GEMS_PER_SLOT;
    }
    
    // Proto
    
    public CharGemSlot toProto() {
        var proto = CharGemSlot.newInstance()
            .setId(this.getId());
        
        for (var gem : this.getGems()) {
            proto.addAlterGems(gem.toProto());
        }
        
        return proto;
    }
}
