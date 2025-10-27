package emu.nebula.command;

import java.util.List;

import emu.nebula.Nebula;
import emu.nebula.game.player.Player;
import emu.nebula.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;

@Getter
public class CommandArgs {
    private String raw;
    private List<String> list;
    private Player sender;
    private Player target;
    
    private int targetUid;
    private int amount;
    private int level = -1;
    private int rank = -1;
    private int promotion = -1;
    private int stage = -1;
    
    private Int2IntMap map;
    private ObjectSet<String> flags;

    public CommandArgs(Player sender, List<String> args) {
        this.sender = sender;
        this.raw = String.join(" ", args);
        this.list = args;
        
        // Parse args. Maybe regex is better.
        var it = this.list.iterator();
        while (it.hasNext()) {
            // Lower case first
            String arg = it.next().toLowerCase();
            
            try {
                if (arg.length() >= 2 && !Character.isDigit(arg.charAt(0)) && Character.isDigit(arg.charAt(arg.length() - 1))) {
                    if (arg.startsWith("@")) { // Target UID
                        this.targetUid = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    } else if (arg.startsWith("x")) { // Amount
                        this.amount = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    } else if (arg.startsWith("lv")) { // Level
                        this.level = Utils.parseSafeInt(arg.substring(2));
                        it.remove();
                    } else if (arg.startsWith("r")) { // Rank
                        this.rank = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    } else if (arg.startsWith("e")) { // Eidolons
                        this.rank = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    } else if (arg.startsWith("p")) { // Promotion
                        this.promotion = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    } else if (arg.startsWith("s")) { // Stage or Superimposition
                        this.stage = Utils.parseSafeInt(arg.substring(1));
                        it.remove();
                    }
                } else if (arg.startsWith("-")) { // Flag
                    if (this.flags == null) this.flags = new ObjectOpenHashSet<>();
                    this.flags.add(arg);
                    it.remove();
                } else if (arg.contains(":") || arg.contains(",")) {
                    String[] split = arg.split("[:,]");
                    if (split.length >= 2) {
                        int key = Integer.parseInt(split[0]);
                        int value = Integer.parseInt(split[1]);
                        
                        if (this.map == null) this.map = new Int2IntOpenHashMap();
                        this.map.put(key, value);
                        
                        it.remove();
                    }
                }
            } catch (Exception e) {
                
            }
        }
        
        // Get target player
        if (targetUid != 0) {
            if (Nebula.getGameContext() != null) {
                target = Nebula.getGameContext().getPlayerModule().getCachedPlayerByUid(targetUid);
            }
        } else {
            target = sender;
        }
        
        if (target != null) {
            this.targetUid = target.getUid();
        }
    }
    
    public int size() {
        return this.list.size();
    }
    
    public String get(int index) {
        if (index < 0 || index >= list.size()) {
            return "";
        }
        
        return this.list.get(index);
    }
    
    /**
     * Sends a message to the command sender
     * @param message
     */
    public void sendMessage(String message) {
        if (sender != null) {
            sender.sendMessage(message);
        } else {
            Nebula.getLogger().info(message);
        }
    }
    
    public boolean hasFlag(String flag) {
        if (this.flags == null) return false;
        return this.flags.contains(flag);
    }
    
}
