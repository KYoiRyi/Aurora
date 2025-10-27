package emu.nebula.game.story;

import dev.morphia.annotations.Entity;
import emu.nebula.database.GameDatabaseObject;
import emu.nebula.game.player.PlayerManager;
import lombok.Getter;

@Getter
@Entity(value = "story", useDiscriminator = false)
public class StoryManager extends PlayerManager implements GameDatabaseObject {

}
