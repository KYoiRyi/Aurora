package emu.nebula.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandResult {
    private CommandHandler command;
    private String message;
    
    public boolean isCommandTypeOf(Class<? extends CommandHandler> handler) {
        if (command == null) {
            return false;
        }
        
        return command.getClass().equals(handler);
    }
}
