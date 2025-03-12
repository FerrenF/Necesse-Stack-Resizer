package stackResizer.commands;

import stackResizer.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.commands.parameterHandlers.BoolParameterHandler;

public class SetDefaultStackSizeModifierCommand extends ModularChatCommand {
    public SetDefaultStackSizeModifierCommand() {
        super("stackresize.stacksize.setdefault", "Set the default stack size modifier for all affected items.", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("stacksize", new IntParameterHandler(), false),
                new CmdParameter("quiet", new BoolParameterHandler(false), true)
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        int stackSize = (int) args[0];
        boolean quiet = (boolean) args[1];
        
        StackResizer.setDefaultStackSizeModifier(stackSize);
        if(quiet) return;
        
        logs.add("Set default stack size modifier to " + stackSize + " for all affected items.");
    }
}
