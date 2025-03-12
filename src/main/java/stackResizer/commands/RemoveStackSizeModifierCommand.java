package stackResizer.commands;

import stackResizer.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.commands.parameterHandlers.BoolParameterHandler;
public class RemoveStackSizeModifierCommand extends ModularChatCommand {
    public RemoveStackSizeModifierCommand() {
        super("stackresize.stacksize.set", "Remove an item or (complete) class name from the blacklist.", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("item", new StringParameterHandler(), false),               
                new CmdParameter("is_class", new BoolParameterHandler(false), true),
                new CmdParameter("quiet", new BoolParameterHandler(false), true),
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        String item = (String) args[0];
        boolean is_class = (boolean)args[1];
        boolean quiet = (boolean)args[2];
        
        int success = is_class ? StackResizer.removeClassStackSizeModifier(item) : StackResizer.removeItemStackSizeModifier(item);
        if(quiet) return;        
        String resultType = is_class ? "class name" : "item name";
        switch(success) {
        	case 1: logs.add("Removed " + resultType + " "+item+" from modifier list. "); break;
        	case 0: logs.add("Failed to remove " + resultType + " from blacklist: not found in blacklist."); break;
        	case -1: logs.add("Failed to remove " + resultType + " from blacklist: type not found."); break;
        	default: logs.add("Failed to remove " + resultType + " from blacklist: error encountered."); break;
        }      
    }
}
