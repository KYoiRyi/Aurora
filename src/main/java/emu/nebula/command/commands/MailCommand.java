package emu.nebula.command.commands;

import emu.nebula.command.Command;
import emu.nebula.command.CommandArgs;
import emu.nebula.command.CommandHandler;
import emu.nebula.game.mail.GameMail;

@Command(label = "mail", aliases = {"m"}, permission = "player.mail", requireTarget = true, desc = "/mail [content]. Sends the targeted player a system mail.")
public class MailCommand implements CommandHandler {

    @Override
    public void execute(CommandArgs args) {
        // Setup mail
        var mail = new GameMail("System", "Test", "This is a test mail");
        
        // Add mail
        args.getTarget().getMailbox().sendMail(mail);
    }

}
