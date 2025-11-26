package emu.nebula.game.dating;

import emu.nebula.data.resources.DatingLandmarkDef;
import emu.nebula.game.character.GameCharacter;
import lombok.Getter;

@Getter
public class DatingGame {
    private GameCharacter character;
    private DatingLandmarkDef landmark;
    
    private int[] branchOptionsA;
    private int[] branchOptionsB;
    
    public DatingGame(GameCharacter character, DatingLandmarkDef landmark) {
        this.character = character;
        this.landmark = landmark;
        this.branchOptionsA = new int[] {1, 2};
        this.branchOptionsB = new int[] {1, 2};
    }
    
    public boolean selectDatingBranchA(int optionId) {
        // TODO
        return true;
    }
    
    public boolean selectDatingBranchB(int optionId) {
        // TODO
        return true;
    }
}
