package emu.nebula.command.commands;

import java.util.Set;

import emu.nebula.GameConstants;
import emu.nebula.command.Command;
import emu.nebula.command.CommandArgs;
import emu.nebula.command.CommandHandler;
import emu.nebula.data.GameData;
import emu.nebula.data.resources.ItemDef;
import emu.nebula.game.inventory.ItemParamMap;
import emu.nebula.game.inventory.ItemSubType;
import emu.nebula.game.player.Player;
import emu.nebula.game.player.PlayerChangeInfo;
import emu.nebula.net.NetMsgId;

@Command(
        label = "giveall", 
        aliases = {"ga"}, 
        permission = "player.give", 
        requireTarget = true, 
        desc = "!ga [characters | discs | materials]. Gives the targeted player items."
)
public class GiveAllCommand implements CommandHandler {
    private static Set<ItemSubType> MATERIAL_ITEM_SUBTYPES = Set.of(
        ItemSubType.DiscStrengthen,         // Disc exp
        ItemSubType.DiscPromote,            // Disc tier up
        ItemSubType.SkillStrengthen,        // Skill upgrade
        ItemSubType.CharacterLimitBreak,    // Character tier up
        ItemSubType.Equipment               // Emblem crafting
    );

    @Override
    public void execute(CommandArgs args) {
        Player target = args.getTarget();
        String type = args.get(0).toLowerCase();
        
        var items = new ItemParamMap();
        var change = new PlayerChangeInfo();

        switch (type) {
            default -> args.sendMessage("Error: Invalid type");
            case "m", "materials", "mats" -> {
                // Check sub type
                for (ItemDef data : GameData.getItemDataTable()) {
                    if (!MATERIAL_ITEM_SUBTYPES.contains(data.getItemSubType())) {
                        continue;
                    }
                    
                    items.add(data.getId(), 10_000);
                }
                
                // Character exp, not sure why this doesnt have an unique sub type so we have to hard code it
                items.add(30001, 10_000);
                items.add(30002, 10_000);
                items.add(30003, 10_000);
                items.add(30004, 10_000);

                // Gold
                items.add(GameConstants.GOLD_ITEM_ID, 50_000_000);

                // Add to target's inventory
                target.getInventory().addItems(items, change);

                // Send message
                args.sendMessage("Giving " + target.getName() + " " + items.size() + " items");
            }
            case "d", "discs" -> {
                // Get all discs
                for (var data : GameData.getDiscDataTable()) {
                    // Skip unavailable discs
                    if (!data.isAvailable() || !data.isVisible()) {
                        continue;
                    }
                    
                    // Check if we have the disc already
                    if (target.getCharacters().hasDisc(data.getId())) {
                        continue;
                    }
                    
                    // Add
                    items.add(data.getId(), 1);
                }
                
                // Add to target's inventory
                target.getInventory().addItems(items, change);

                // Send message
                args.sendMessage("Giving " + target.getName() + " all discs");
            }
            case "c", "characters", "trekkers", "t" -> {
                // Get all characters
                for (var data : GameData.getCharacterDataTable()) {
                    // Skip unavailable characters
                    if (!data.isAvailable() || !data.isVisible()) {
                        continue;
                    }
                    
                    // Check if we have the character already
                    if (target.getCharacters().hasCharacter(data.getId())) {
                        continue;
                    }
                    
                    // Add
                    items.add(data.getId(), 1);
                }
                
                // Add to target's inventory
                target.getInventory().addItems(items, change);

                // Send message
                args.sendMessage("Giving " + target.getName() + " all characters");
            }
        }
        
        if (change.isEmpty()) {
            return;
        }
        
        // Encode and send
        target.addNextPackage(NetMsgId.items_change_notify, change.toProto());
    }

}
