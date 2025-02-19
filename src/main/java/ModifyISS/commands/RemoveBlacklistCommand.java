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
public class RemoveBlacklistCommand extends ModularChatCommand {
	public RemoveBlacklistCommand() {
		super("stackresize.blacklist.remove", "Remove an item or class from the blacklist..", PermissionLevel.OWNER, false,new CmdParameter[]{
				new CmdParameter("item", new StringParameterHandler(),false),
				new CmdParameter("is_class", new BoolParameterHandler(false),true),
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {			
		int result = (boolean)args[2] ? StackResizer.removeClassFromBlacklist((String)args[0]) :
			StackResizer.removeItemFromBlacklist((String)args[0]);
		
		String resultType = (boolean)args[2] ? "class name" : "item string ID";
		if (result > 0){
			logs.add("Removed "+resultType+" from blacklist .");
		}
		else if( result == -1){
			logs.add("Could not remove from blacklist: "+resultType+" not found.");
		}
		else if( result == 0){
			logs.add("Could not remove from blacklist: "+resultType+" is not on the blacklist.");
		}
	}
}