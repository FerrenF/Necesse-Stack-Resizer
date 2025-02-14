package main.java.ModifyISS.commands;

import main.java.ModifyISS.ModifyISS;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;

public class SetDefaultStackSizeModifierCommand extends ModularChatCommand {
    public SetDefaultStackSizeModifierCommand() {
        super("iss.stacksize.setdefault", "Set the default stack size modifier for all affected items.", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("stacksize", new IntParameterHandler(), false)
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        int stackSize = (int) args[0];
        
        boolean success = ModifyISS.setDefaultStackSizeModifier(stackSize);
        if (success) {
            logs.add("Set default stack size modifier to " + stackSize + " for all affected items.");
        } else {
            logs.add("Failed to set default stack size modifier. Check if the value is valid.");
        }
    }
}
