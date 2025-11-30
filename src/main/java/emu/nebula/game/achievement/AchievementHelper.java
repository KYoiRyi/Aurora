package emu.nebula.game.achievement;

import java.util.List;

import emu.nebula.data.GameData;
import emu.nebula.data.resources.AchievementDef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

// Because achievements in the data files do not have params, we will hardcode them here
public class AchievementHelper {
    // Achievement cache
    
    @Getter
    private static Int2ObjectMap<List<AchievementDef>> cache = new Int2ObjectOpenHashMap<>();
    
    public static List<AchievementDef> getAchievementsByCondition(AchievementCondition condition) {
        return cache.get(condition.getValue());
    }
    
    // Fix params
    
    public static void fixParams() {
        addParam(78, -1, 2);
        addParam(79, -1, 4);
        addParam(498, -1, 1);
    }
    
    private static void addParam(int achievementId, int param1, int param2) {
        var data = GameData.getAchievementDataTable().get(achievementId);
        if (data == null) return;
        
        data.setParams(param1, param2);
    }
}
