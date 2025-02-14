package main.java.ModifyISS.commands;

import main.java.ModifyISS.ModifyISS;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.commands.parameterHandlers.BoolParameterHandler;
public class SetStackSizeModifierCommand extends ModularChatCommand {
    public SetStackSizeModifierCommand() {
        super("iss.stacksize.set", "Set the stack size modifier for an item.", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("item", new StringParameterHandler(), false),
                new CmdParameter("stacksize", new IntParameterHandler(), false),
                new CmdParameter("is_class", new BoolParameterHandler(false), true),
                new CmdParameter("quiet", new BoolParameterHandler(false), true),
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        String item = (String) args[0];
        int stackSize = (int) args[1];
        boolean is_class = (boolean)args[2];
        boolean quiet = (boolean)args[3];
        
        int success = is_class ? ModifyISS.setClassStackSizeModifier(item, stackSize) : ModifyISS.setItemStackSizeModifier(item, stackSize);
        if(quiet) return;
        
        String resultType = is_class ? "class name" : "item name";
        if (success == 1) {
            logs.add("Set stack size modifier for " + resultType + " "+item+" to " + stackSize + ".");
        } else {
            logs.add("Failed to set stack size modifier for " + resultType + ". It does not exist.");
        }
    }
}
