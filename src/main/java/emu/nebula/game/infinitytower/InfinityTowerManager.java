package emu.nebula.game.infinitytower;

import emu.nebula.data.GameData;
import emu.nebula.data.resources.InfinityTowerLevelDef;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerChangeInfo;
import emu.nebula.game.player.PlayerManager;

import lombok.Getter;

@Getter
public class InfinityTowerManager extends PlayerManager {
    private InfinityTowerLevelDef levelData;
    private int levelId;
    
    private long buildId;
    
    public InfinityTowerManager(Player player) {
        super(player);
    }
    
    public int getBountyLevel() {
        return 1;
    }
    
    public boolean apply(int levelId, long buildId) {
        // Verify level data
        var data = GameData.getInfinityTowerLevelDataTable().get(levelId);
        if (data == null) {
            return false;
        }
        
        // Set level id
        this.levelData = data;
        this.levelId = levelId;
        
        // Set build id
        if (buildId >= 0) {
            this.buildId = buildId;
        }
        
        // Success
        return true;
    }

    public PlayerChangeInfo settle(int value) {
        // Verify level data
        if (this.getLevelData() == null) {
            return null;
        }
        
        // Init change info
        var change = new PlayerChangeInfo();
        
        // Check if the player has won or not TODO
        if (value != 1) {
            // Player lost, so we return nothing
            return change;
        }
        
        // Calculate rewards
        var rewards = this.getLevelData().generateRewards();
        
        // Add items
        this.getPlayer().getInventory().addItems(rewards, change);
        
        // Set in change info
        change.setExtraData(rewards);
        
        // Log in player progress
        this.getPlayer().getProgress().addInfinityArenaLog(this.getLevelId());
        
        // Success
        return change.setSuccess(true);
    }

}
