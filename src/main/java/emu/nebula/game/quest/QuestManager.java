package emu.nebula.game.quest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import emu.nebula.Nebula;
import emu.nebula.data.GameData;
import emu.nebula.database.GameDatabaseObject;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerChangeInfo;
import emu.nebula.game.player.PlayerManager;
import emu.nebula.net.NetMsgId;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;

@Getter
@Entity(value = "quests", useDiscriminator = false)
public class QuestManager extends PlayerManager implements GameDatabaseObject {
    @Id
    private int uid;
    
    // Daily activity missions
    private int activity;
    private IntSet claimedActiveIds;
    
    // Quests
    private Map<Integer, GameQuest> quests;
    
    @Deprecated // Morphia only
    public QuestManager() {
        
    }
    
    public QuestManager(Player player) {
        super(player);
        this.uid = player.getUid();
        this.claimedActiveIds = new IntOpenHashSet();
        this.quests = new HashMap<>();
        
        this.resetDailyQuests();
        
        this.save();
    }
    
    public synchronized void resetDailyQuests() {
        // Reset daily quests
        for (var data : GameData.getDailyQuestDataTable()) {
            // Get quest
            var quest = getQuests().computeIfAbsent(data.getId(), i -> new GameQuest(data));
            
            // Reset progress
            quest.resetProgress();
            
            // Sync quest with player client
            this.syncQuest(quest);
        }
        
        // Reset activity
        this.activity = 0;
        this.claimedActiveIds.clear();
        
        // Persist to database
        this.save();
    }

    public synchronized void triggerQuest(QuestCondType condition, int param) {
        for (var quest : getQuests().values()) {
            // Try to trigger quest
            boolean result = quest.trigger(condition, param);
            
            // Skip if quest progress wasn't changed
            if (!result) {
                continue;
            }
            
            // Sync quest with player client
            this.syncQuest(quest);
            
            // Update in database
            Nebula.getGameDatabase().update(this, this.getUid(), "quests." + quest.getId(), quest);
        }
    }
    
    public PlayerChangeInfo receiveReward(int questId) {
        // Get received quests
        var claimList = new HashSet<GameQuest>();
        
        if (questId > 0) {
            // Claim specific quest
            var quest = this.getQuests().get(questId);
            
            if (quest != null && !quest.isClaimed()) {
                claimList.add(quest);
            }
        } else {
            // Claim all
            for (var quest : this.getQuests().values()) {
                if (!quest.isComplete() || quest.isClaimed()) {
                    continue;
                }
                
                claimList.add(quest);
            }
        }
        
        // Sanity check
        if (claimList.isEmpty()) {
            return null;
        }

        // Create change info
        var change = new PlayerChangeInfo();
        
        // Claim
        for (var quest : claimList) {
            // Get data
            var data = GameData.getDailyQuestDataTable().get(quest.getId());
            if (data != null) {
                // Add reward data
                this.getPlayer().getInventory().addItem(data.getItemTid(), data.getItemQty(), change);
                
                // Add activity
                this.activity += data.getActive();
            }
            
            // Set claimed
            quest.setClaimed(true);
            
            // Update in database
            Nebula.getGameDatabase().update(this, this.getUid(), "quests." + quest.getId(), quest);
        }
        
        // Update in database
        Nebula.getGameDatabase().update(this, this.getUid(), "activity", this.getActivity());
        
        // Success
        return change;
    }
    
    public PlayerChangeInfo claimActiveRewards() {
        // Init
        var claimList = new IntArrayList();
        var rewards = new ItemParamMap();
        
        // Get claimable 
        for (var data : GameData.getDailyQuestActiveDataTable()) {
            if (this.getClaimedActiveIds().contains(data.getId())) {
                continue;
            }
            
            if (this.getActivity() >= data.getActive()) {
                // Add rewards
                rewards.add(data.getRewards());
                
                // Add to claimed activity list
                claimList.add(data.getId());
            }
        }
        
        // Sanity check
        if (claimList.isEmpty()) {
            return null;
        }
        
        // Add rewards
        var change = this.getPlayer().getInventory().addItems(rewards);
        
        // Set claimed list
        change.setExtraData(claimList);
        
        // Update in database
        this.getClaimedActiveIds().addAll(claimList);
        Nebula.getGameDatabase().update(this, this.getUid(), "claimedActiveIds", this.getClaimedActiveIds());
        
        // Success
        return change;
    }
    
    /**
     * Update this quest on the player client
     */
    private void syncQuest(GameQuest quest) {
        if (!getPlayer().hasSession()) {
            return;
        }
        
        getPlayer().addNextPackage(
                NetMsgId.quest_change_notify, 
                quest.toProto()
        );
    }
}
