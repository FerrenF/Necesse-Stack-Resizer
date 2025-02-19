package main.java.ModifyISS.commands;

import main.java.ModifyISS.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.commands.parameterHandlers.BoolParameterHandler;

public class AddAllBlacklistCommand extends ModularChatCommand {
    public AddAllBlacklistCommand() {
        super("stackresize.blacklist.addall", "Bulk set blacklist items. They must all be either items or classnames. Format: item;item;", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("formattedstring", new StringParameterHandler(), false),
                new CmdParameter("is_class", new BoolParameterHandler(false), true),
                new CmdParameter("quiet", new BoolParameterHandler(false), true),
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        String formattedstring = (String) args[0];
        boolean is_class = (boolean)args[1];
        boolean quiet = (boolean)args[2];
        
        int success = is_class ? StackResizer.setBlacklistClassesFromString(formattedstring) : StackResizer.setBlacklistItemsFromString(formattedstring);
        if(quiet) return;
        
        String resultType = is_class ? "class names" : "item names";
        if (success == 1) {
        	logs.add(String.format("Added %d %s to blacklist. Use /stackresize.blacklist to see changes.", success, resultType));
        			
        } else {
        	logs.add(String.format("Failed to add %s to blacklist.", resultType));
        }
    }
}
