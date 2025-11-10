package emu.nebula.game.player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import emu.nebula.Nebula;
import emu.nebula.game.GameContext;
import emu.nebula.game.GameContextModule;
import emu.nebula.game.account.Account;
import emu.nebula.net.GameSession;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class PlayerModule extends GameContextModule {
    private final Int2ObjectMap<Player> cachedPlayers;
    private final Object2ObjectMap<String, Player> cachedPlayersByAccount;

    public PlayerModule(GameContext gameContext) {
        super(gameContext);

        this.cachedPlayers = new Int2ObjectOpenHashMap<>();
        this.cachedPlayersByAccount = new Object2ObjectOpenHashMap<>();
    }

    public Int2ObjectMap<Player> getCachedPlayers() {
        return cachedPlayers;
    }
    
    private void addToCache(Player player) {
        this.cachedPlayers.put(player.getUid(), player);
        this.cachedPlayersByAccount.put(player.getAccountUid(), player);
    }
    
    public void removeFromCache(Player player) {
        this.cachedPlayers.remove(player.getUid());
        this.cachedPlayersByAccount.remove(player.getAccountUid());
    }

    /**
     * Returns a player object that has been previously cached. Returns null if the player isnt in the cache.
     * @param uid User id of the player
     * @return
     */
    public synchronized Player getCachedPlayerByUid(int uid) {
        return getCachedPlayers().get(uid);
    }
    
    /**
     * Returns a player object with the given account. Returns null if the player doesnt exist.
     * @param uid User id of the player
     * @return
     */
    public synchronized Player getPlayerByAccount(Account account) {
        // Get player from cache
        Player player = this.cachedPlayersByAccount.get(account.getUid());

        if (player == null) {
            // Retrieve player object from database if its not there
            player = Nebula.getGameDatabase().getObjectByField(Player.class, "accountUid", account.getUid());

            if (player != null) {
                // Load player
                player.onLoad();

                // Put in cache
                this.addToCache(player);
            }
        }

        return player;
    }

    /**
     * Creates a player with the specified user id.
     * @param userId
     * @return
     */
    public synchronized Player createPlayer(GameSession session, String name, boolean gender) {
        // Make sure player doesnt already exist
        if (Nebula.getGameDatabase().checkIfObjectExists(Player.class, "accountUid", session.getAccount().getUid())) {
            return null;
        }
        
        // Limit name length
        if (name.length() > 20) {
            name = name.substring(0, 19);
        }

        // Create player and save to db
        var player = new Player(session.getAccount(), name, gender);
        player.onLoad();
        player.save();
        
        // Send welcome mail
        player.getMailbox().sendWelcomeMail();
        
        // Put in player cache
        this.addToCache(player);
        
        // Set player for session
        session.setPlayer(player);

        return player;
    }
    
    /**
     * Returns a list of recent players that have logged on (for followers)
     * @param player Player that requested this
     */
    public synchronized List<Player> getRandomPlayerList(Player player) {
        List<Player> list = getCachedPlayers().values().stream().filter(p -> p != player).collect(Collectors.toList());
        Collections.shuffle(list);
        return list.stream().limit(15).toList();
    }
}
