package emu.nebula.game.vampire;

import java.util.stream.IntStream;

import emu.nebula.GameConstants;
import emu.nebula.data.resources.VampireSurvivorDef;
import emu.nebula.proto.VampireSurvivorSettle.VampireSurvivorAreaInfo;
import lombok.Getter;

@Getter
public class VampireSurvivorArea {
    private final VampireSurvivorGame game;
    
    private int time;
    private int score;
    
    private int[] killCount;
    private int[] killScore;
    
    public VampireSurvivorArea(VampireSurvivorGame game, int time, int[] killCount) {
        this.game = game;
        this.time = time;
        
        this.calcScore(killCount);
    }
    
    private VampireSurvivorDef getData() {
        return this.getGame().getData();
    }
    
    private void calcScore(int[] killCount) {
        // Set kill count/score
        this.killCount = killCount;
        this.killScore = new int[killCount.length];
        
        // Check size for kill counter array
        int maxSize = 4 + (GameConstants.VAMPIRE_SURVIVOR_BONUS_POWER.length * 2);
        
        if (killCount.length < maxSize) {
            return;
        }
        
        // Calculate
        killScore[0] = killCount[0] * this.getData().getNormalScore1(); // Monster
        killScore[1] = killCount[1] * this.getData().getEliteScore1();  // Elite monster
        killScore[2] = killCount[2] * this.getData().getEliteScore1();  // Lord
        killScore[3] = this.getData().getBossScore1();                  // Boss
        
        int offset = 4;
        
        for (int i = 0; i < GameConstants.VAMPIRE_SURVIVOR_BONUS_POWER.length; i++, offset++) {
            int bonusKill = killCount[offset];
            int bonusPower = GameConstants.VAMPIRE_SURVIVOR_BONUS_POWER[i][1];
            double mod = (bonusPower - 100) / 100D;
            
            killScore[offset] = (int) (bonusKill * mod * this.getData().getNormalScore1());
        }
        
        for (int i = 0; i < GameConstants.VAMPIRE_SURVIVOR_BONUS_POWER.length; i++, offset++) {
            int bonusKill = killCount[offset];
            int bonusPower = GameConstants.VAMPIRE_SURVIVOR_BONUS_POWER[i][1];
            double mod = (bonusPower - 100) / 100D;
            
            killScore[offset] = (int) (bonusKill * mod * this.getData().getEliteScore1());
        }
        
        // Get final score
        this.score = IntStream.of(killScore).sum();
    }
    
    // Proto

    public VampireSurvivorAreaInfo toProto() {
        var proto = VampireSurvivorAreaInfo.newInstance()
                .setBossTime(this.getTime())
                .setScore(this.getScore())
                .addAllKillCount(this.getKillCount())
                .addAllKillScore(this.getKillScore());
        
        return proto;
    }
}
