package emu.nebula.game.player;

public abstract class PlayerManager {
    private transient Player player;

    public PlayerManager() {
        
    }
    
    public PlayerManager(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }
    
    public void setPlayer(Player player) {
        if (this.player == null) {
            this.player = player;
        }
    }

    public int getPlayerUid() {
        return this.getPlayer().getUid();
    }
}
