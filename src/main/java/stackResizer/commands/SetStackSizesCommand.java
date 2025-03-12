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

public class SetStackSizesCommand extends ModularChatCommand {
    public SetStackSizesCommand() {
        super("stackresize.stacksize.setall", "Bulk set stack sizes. They must all be either items or classnames. Format: item=stacksize;item=stacksize;", PermissionLevel.OWNER, false, new CmdParameter[]{
                new CmdParameter("formatted_string", new StringParameterHandler(), false),
                new CmdParameter("is_class", new BoolParameterHandler(false), true),
                new CmdParameter("quiet", new BoolParameterHandler(false), true),
        });
    }

    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
        String formattedstring = (String) args[0];
        boolean is_class = (boolean)args[1];
        boolean quiet = (boolean)args[2];
        
        int success = is_class ? StackResizer.setClassStackSizeModifiersFromString(formattedstring) : StackResizer.setItemStackSizeModifiersFromString(formattedstring);
        if(quiet) return;
        
        String resultType = is_class ? "class names" : "item names";
        if (success > 0) {
        	logs.add(String.format("Set stack size modifier for %d %s. Use /stackresize.stacksize to see changes.", success, resultType));
        			
        } else {
        	logs.add(String.format("Failed to set stack size modifier for %d %s.", success, resultType));
        }
    }
}
