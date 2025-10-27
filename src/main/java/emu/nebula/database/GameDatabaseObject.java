package emu.nebula.database;

import emu.nebula.Nebula;

public interface GameDatabaseObject {

    public default void save() {
        Nebula.getGameDatabase().save(this);
    }
    
}
