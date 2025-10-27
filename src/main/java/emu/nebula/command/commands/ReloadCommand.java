package emu.nebula.command.commands;

import emu.nebula.Nebula;
import emu.nebula.command.Command;
import emu.nebula.command.CommandArgs;
import emu.nebula.command.CommandHandler;

@Command(label = "reload", permission = "admin.reload", desc = "/reload. Reloads the server config.")
public class ReloadCommand implements CommandHandler {

    @Override
    public void execute(CommandArgs args) {
        Nebula.loadConfig();
        args.sendMessage("Reloaded the server config");
    }

}
