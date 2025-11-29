package emu.nebula.data.resources;

import java.util.List;

import emu.nebula.data.BaseDef;
import emu.nebula.data.ResourceType;
import emu.nebula.game.character.GameCharacter;
import emu.nebula.game.inventory.ItemRewardList;
import emu.nebula.game.inventory.ItemRewardParam;
import emu.nebula.util.JsonUtils;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

@Getter
@ResourceType(name = "Agent.json")
public class AgentDef extends BaseDef {
    private int Id;
    private int Level;
    private int MemberLimit;
    private int[] Tags;
    private int[] ExtraTags;
    
    private int Time1;
    private String RewardPreview1;
    private String BonusPreview1;
    
    private int Time2;
    private String RewardPreview2;
    private String BonusPreview2;
    
    private int Time3;
    private String RewardPreview3;
    private String BonusPreview3;
    
    private int Time4;
    private String RewardPreview4;
    private String BonusPreview4;
    
    private transient Int2ObjectMap<AgentDurationDef> durations;
    private transient Int2IntOpenHashMap tags;
    private transient Int2IntOpenHashMap extraTags;
    
    @Override
    public int getId() {
        return Id;
    }
    
    private void addDuration(AgentDurationDef duration) {
        this.durations.put(duration.getTime(), duration);
    }
    
    public boolean hasTags(List<GameCharacter> characters) {
        // Get character tags
        var characterTags = new Int2IntOpenHashMap();
        
        for (var character : characters) {
            var data = character.getData().getDes();
            
            for (int tag : data.getTag()) {
                characterTags.addTo(tag, 1);
            }
        }
        
        // Validate that we have the tags
        for (var entry : this.tags.int2IntEntrySet()) {
            int reqTagId = entry.getIntKey();
            int reqTagCount = entry.getIntValue();
            
            // Get amount of tags that we have from our characters
            int characterTagCount = characterTags.get(reqTagId);
            
            if (reqTagCount > characterTagCount) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasExtraTags(List<GameCharacter> characters) {
        // Get character tags
        var characterTags = new Int2IntOpenHashMap();
        
        for (var character : characters) {
            var data = character.getData().getDes();
            
            for (int tag : data.getTag()) {
                characterTags.addTo(tag, 1);
            }
        }
        
        // Validate that we have the tags
        for (var entry : this.extraTags.int2IntEntrySet()) {
            int reqTagId = entry.getIntKey();
            int reqTagCount = entry.getIntValue();
            
            // Get amount of tags that we have from our characters
            int characterTagCount = characterTags.get(reqTagId);
            
            if (reqTagCount > characterTagCount) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void onLoad() {
        // Cache durations
        this.durations = new Int2ObjectOpenHashMap<>();
        
        this.addDuration(new AgentDurationDef(this.Time1, this.RewardPreview1, this.BonusPreview1));
        this.addDuration(new AgentDurationDef(this.Time2, this.RewardPreview2, this.BonusPreview2));
        this.addDuration(new AgentDurationDef(this.Time3, this.RewardPreview3, this.BonusPreview3));
        this.addDuration(new AgentDurationDef(this.Time4, this.RewardPreview4, this.BonusPreview4));
        
        // Cache tags
        this.tags = new Int2IntOpenHashMap();
        this.extraTags = new Int2IntOpenHashMap();
        
        for (int tag : this.Tags) {
            this.tags.addTo(tag, 1);
        }
        
        for (int tag : this.ExtraTags) {
            this.extraTags.addTo(tag, 1);
        }
    }
    
    @Getter
    public static class AgentDurationDef {
        private int time;
        private ItemRewardList rewards;
        private ItemRewardList bonus;
        
        public AgentDurationDef(int time, String rewardPreview, String bonusPreview) {
            this.time = time;
            this.rewards = new ItemRewardList();
            this.bonus = new ItemRewardList();
            
            var rewardArray = JsonUtils.decodeList(rewardPreview, int[].class);
            if (rewardArray != null) {
                for (int[] award : rewardArray) {
                    this.rewards.add(new ItemRewardParam(award[0], award[1], award[2]));
                }
            }
            
            var bonusArray = JsonUtils.decodeList(bonusPreview, int[].class);
            if (bonusArray != null) {
                for (int[] award : bonusArray) {
                    this.bonus.add(new ItemRewardParam(award[0], award[1], award[2]));
                }
            }
        }
    }
}
