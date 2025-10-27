package emu.nebula.game;

public abstract class GameContextModule {
    private transient GameContext context;

    public GameContextModule(GameContext player) {
        this.context = player;
    }

    public GameContext getGameContext() {
        return context;
    }
}
