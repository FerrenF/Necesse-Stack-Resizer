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
public class AddBlacklistCommand extends ModularChatCommand {
	public AddBlacklistCommand() {
		super("stackresize.blacklist.add", "Add an item to the blacklist.", PermissionLevel.OWNER, false,new CmdParameter[]{
				new CmdParameter("item", new StringParameterHandler(),false),
				new CmdParameter("is_class", new BoolParameterHandler(false),true),
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {			
		int result = (boolean)args[2] ? StackResizer.addClassToBlacklist((String)args[0]) :
			StackResizer.addItemToBlacklist((String)args[0]);
		
		String resultType = (boolean)args[2] ? "class name" : "item string ID";
		if (result > 0){
			logs.add("Added "+resultType+" to blacklist .");
		}
		else if( result == -1){
			logs.add("Could not add to blacklist: "+resultType+" not found.");
		}
		else if( result == 0){
			logs.add("Could not add to blacklist: "+resultType+" already on blacklist.");
		}
	}
}