package emu.nebula.game.instance;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import emu.nebula.GameConstants;
import emu.nebula.Nebula;
import emu.nebula.data.GameData;
import emu.nebula.database.GameDatabaseObject;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerChangeInfo;
import emu.nebula.game.player.PlayerManager;
import emu.nebula.game.quest.QuestCondType;
import emu.nebula.proto.PlayerData.PlayerInfo;
import emu.nebula.proto.Public.CharGemInstance;
import emu.nebula.proto.Public.DailyInstance;
import emu.nebula.proto.Public.RegionBossLevel;
import emu.nebula.proto.Public.SkillInstance;
import emu.nebula.proto.Public.WeekBossLevel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.Getter;

@Getter
@Entity(value = "instances", useDiscriminator = false)
public class InstanceManager extends PlayerManager implements GameDatabaseObject {
    @Id
    private int uid;
    
    private Int2IntMap dailyInstanceLog;
    private Int2IntMap regionBossLog;
    private Int2IntMap skillInstanceLog;
    private Int2IntMap charGemLog;
    private Int2IntMap weekBossLog;
    
    private transient int curInstanceId;
    private transient int rewardType;
    
    @Deprecated // Morphia
    public InstanceManager() {
        
    }
    
    public InstanceManager(Player player) {
        super(player);
        this.uid = player.getUid();
        
        this.dailyInstanceLog = new Int2IntOpenHashMap();
        this.regionBossLog = new Int2IntOpenHashMap();
        this.skillInstanceLog = new Int2IntOpenHashMap();
        this.charGemLog = new Int2IntOpenHashMap();
        this.weekBossLog = new Int2IntOpenHashMap();
        
        this.save();
    }
    
    public void setCurInstanceId(int id) {
        this.setCurInstanceId(id, 0);
    }

    public void setCurInstanceId(int id, int rewardType) {
        this.curInstanceId = id;
        this.rewardType = rewardType;
    }
    
    public void saveInstanceLog(Int2IntMap log, String logName, int id, int newStar) {
        // Get current star
        int star = log.get(id);
        
        // Check star
        if (newStar <= star || newStar > 7) {
            return;
        }
        
        // Add to log and update database
        log.put(id, newStar);
        Nebula.getGameDatabase().update(this, this.getUid(), logName + "." + id, newStar);
    }
    
    public PlayerChangeInfo settleInstance(InstanceData data, QuestCondType questCondition, Int2IntMap log, String logName, int star) {
        // Calculate settle data
        var settleData = new InstanceSettleData();
        
        settleData.setWin(star > 0);
        settleData.setFirst(settleData.isWin() && !log.containsKey(data.getId()));
        
        // Init player change info
        var change = new PlayerChangeInfo();
        
        // Handle win
        if (settleData.isWin()) {
            // Reward type
            int rewardType = this.getRewardType();
            
            // Calculate energy and exp
            settleData.setExp(data.getEnergyConsume());
            getPlayer().consumeEnergy(settleData.getExp(), change);
            
            // Awards
            getPlayer().getInventory().addItem(GameConstants.EXP_ITEM_ID, settleData.getExp(), change);
            getPlayer().getInventory().addItems(data.getRewards(rewardType), change);
            
            if (settleData.isFirst()) {
                getPlayer().getInventory().addItems(data.getFirstRewards(rewardType), change);
            }
            
            // Log
            this.saveInstanceLog(log, logName, data.getId(), star);
            
            // Quest triggers
            this.getPlayer().getQuestManager().triggerQuest(questCondition, 1);
            this.getPlayer().getQuestManager().triggerQuest(QuestCondType.BattleTotal, 1);
        }
        
        // Set extra data
        change.setExtraData(settleData);
        
        // Success
        return change.setSuccess(true);
    }
    
    public PlayerChangeInfo sweepInstance(InstanceData data, QuestCondType questCondition, Int2IntMap log, int rewardType, int count) {
        // Sanity check count
        if (count <= 0) {
            return null;
        }
        
        // Check if we have 3 starred this instance
        int stars = log.get(data.getId());
        
        if (rewardType > 0) {
            // Daily instance
            if (stars != 7) {
                return null;
            }
        } else {
            // Other instances
            if (stars != 3) {
                return null;
            }
        }
        
        // Check energy cost
        int energyCost = data.getEnergyConsume() * count;
        
        if (this.getPlayer().getEnergy() < energyCost) {
            return null;
        }
        
        // Init player change info
        var change = new PlayerChangeInfo();
        
        // Consume exp
        getPlayer().consumeEnergy(energyCost, change);
        
        // Awards
        getPlayer().getInventory().addItem(GameConstants.EXP_ITEM_ID, energyCost, change);
        getPlayer().getInventory().addItems(data.getRewards(rewardType).mulitply(count), change);
        
        // Quest triggers
        this.getPlayer().getQuestManager().triggerQuest(questCondition, count);
        this.getPlayer().getQuestManager().triggerQuest(QuestCondType.BattleTotal, count);
        
        // Success
        return change.setSuccess(true);
    }

    // Proto
    
    public void toProto(PlayerInfo proto) {
        // Init
        int minStars = 0;
        
        // Simple hack to unlock all instances
        if (Nebula.getConfig().getServerOptions().unlockInstances) {
            minStars = 1;
        }
        
        // Daily instance
        for (var data : GameData.getDailyInstanceDataTable()) {
            int stars = Math.max(getDailyInstanceLog().get(data.getId()), minStars);
            
            var p = DailyInstance.newInstance()
                    .setId(data.getId())
                    .setStar(stars);
            
            proto.addDailyInstances(p);
        }
        
        // Regional boss
        for (var data : GameData.getRegionBossLevelDataTable()) {
            int stars = Math.max(getRegionBossLog().get(data.getId()), minStars);
            
            var p = RegionBossLevel.newInstance()
                    .setId(data.getId())
                    .setStar(stars);
            
            proto.addRegionBossLevels(p);
        }
        
        // Skill instance
        for (var data : GameData.getSkillInstanceDataTable()) {
            int stars = Math.max(getSkillInstanceLog().get(data.getId()), minStars);
            
            var p = SkillInstance.newInstance()
                    .setId(data.getId())
                    .setStar(stars);
            
            proto.addSkillInstances(p);
        }
        
        // Char gem instance
        for (var data : GameData.getCharGemInstanceDataTable()) {
            int stars = Math.max(getCharGemLog().get(data.getId()), minStars);
            
            var p = CharGemInstance.newInstance()
                    .setId(data.getId())
                    .setStar(stars);
            
            proto.addCharGemInstances(p);
        }
        
        // Weekly boss
        for (var data : GameData.getWeekBossLevelDataTable()) {
            var p = WeekBossLevel.newInstance()
                    .setId(data.getId())
                    .setFirst(this.getWeekBossLog().get(data.getId()) == 1);
            
            proto.addWeekBossLevels(p);
        }
    }
}
