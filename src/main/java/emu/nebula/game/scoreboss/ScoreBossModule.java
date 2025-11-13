package emu.nebula.game.scoreboss;

import java.util.ArrayList;
import java.util.List;

import emu.nebula.Nebula;
import emu.nebula.game.GameContext;
import emu.nebula.game.GameContextModule;
import emu.nebula.proto.ScoreBossRank.ScoreBossRankData;
import lombok.Getter;

@Getter
public class ScoreBossModule extends GameContextModule {
    private long lastUpdate;
    private long nextUpdate;
    private List<ScoreBossRankData> ranking;
    
    public ScoreBossModule(GameContext context) {
        super(context);
        this.nextUpdate = -1;
        this.ranking = new ArrayList<>();
    }
    
    public synchronized List<ScoreBossRankData> getRanking() {
        if (System.currentTimeMillis() > this.nextUpdate) {
            this.updateRanking();
        }
        
        return this.ranking;
    }

    // Cache ranking so we dont query the database too much
    private void updateRanking() {
        // Clear
        this.ranking.clear();
        
        // Get from database
        var list = Nebula.getGameDatabase().getSortedObjects(ScoreBossRankEntry.class, "score", 50);
        
        for (int i = 0; i < list.size(); i++) {
            // Get rank entry and set proto
            var entry = list.get(i);
            entry.setRank(i + 1);
            
            // Add to ranking
            this.ranking.add(entry.toProto());
        }
        
        this.nextUpdate = System.currentTimeMillis() + 1000;
        this.lastUpdate = Nebula.getCurrentTime();
    }
}
