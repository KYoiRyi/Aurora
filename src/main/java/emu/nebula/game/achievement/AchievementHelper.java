package emu.nebula.game.achievement;

import java.util.List;

import emu.nebula.GameConstants;
import emu.nebula.data.GameData;
import emu.nebula.data.resources.AchievementDef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;

// Because achievements in the data files do not have params, we will hardcode them here
public class AchievementHelper {
    // Cache
    private static IntSet incrementalAchievementSet = new IntOpenHashSet();
    
    @Getter
    private static Int2ObjectMap<List<AchievementDef>> cache = new Int2ObjectOpenHashMap<>();
    
    public static List<AchievementDef> getAchievementsByCondition(int condition) {
        return cache.get(condition);
    }
    
    //
    
    public static boolean isIncrementalAchievement(int condition) {
        return incrementalAchievementSet.contains(condition);
    }
    
    // Fix params
    
    public static void init() {
        // Cache total achievements
        for (var condition : AchievementCondition.values()) {
            if (condition.name().endsWith("Total")) {
                incrementalAchievementSet.add(condition.getValue());
            }
        }
        
        incrementalAchievementSet.remove(AchievementCondition.AchievementTotal.getValue());
        
        incrementalAchievementSet.add(AchievementCondition.ItemsAdd.getValue());
        incrementalAchievementSet.add(AchievementCondition.ItemsDeplete.getValue());
        
        // Fix params
        fixParams();
    }
    
    private static void fixParams() {
        // Monolith
        addParam(78, 0, 2);
        addParam(79, 0, 4);
        addParam(498, 0, 1);
        
        // Money
        addParam(25, GameConstants.GOLD_ITEM_ID, 0);
        addParam(26, GameConstants.GOLD_ITEM_ID, 0);
        addParam(27, GameConstants.GOLD_ITEM_ID, 0);
        addParam(28, GameConstants.GOLD_ITEM_ID, 0);
        addParam(29, GameConstants.GOLD_ITEM_ID, 0);
        
        // Ininfite tower
        for (int diff = 10, id = 270; diff <= 60; diff += 10) {
            addParam(id++, 11000 + diff, 0); // Infinite Arena
            addParam(id++, 51000 + diff, 0); // Shake the Floor
            addParam(id++, 41000 + diff, 0); // Elegance and Flow
            addParam(id++, 71000 + diff, 0); // Upbeat Party
            addParam(id++, 31000 + diff, 0); // Thrilling Beat
            addParam(id++, 21000 + diff, 0); // Flames and Beats
            addParam(id++, 61000 + diff, 0); // Sinister Ritual
        }
        
        // Character count
        addParam(393, 1, 0);
        addParam(394, 1, 0);
        addParam(395, 1, 0);
        addParam(396, 1, 0);
        addParam(397, 1, 0);
        addParam(398, 1, 0);
        
        // Disc count
        addParam(382, 1, 0);
        addParam(383, 1, 0);
        addParam(384, 1, 0);
        addParam(385, 1, 0);
        addParam(386, 1, 0);
        addParam(387, 1, 0);
    }
    
    private static void addParam(int achievementId, int param1, int param2) {
        var data = GameData.getAchievementDataTable().get(achievementId);
        if (data == null) return;
        
        data.setParams(param1, param2);
    }
}
