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
import necesse.engine.commands.parameterHandlers.BoolParameterHandler;
public class GetStackSizeModifierCommand extends ModularChatCommand {
	public GetStackSizeModifierCommand() {
		super("stackresize.stacksize.get", "Get the stack size for an item.", PermissionLevel.USER, false, new CmdParameter[]{
				new CmdParameter("item", new StringParameterHandler(),false),
				new CmdParameter("is_class", new BoolParameterHandler(false),true),
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {		
		
		String target = (String)args[0];
		boolean is_class = (boolean)args[1];
		boolean quiet = (boolean)args[2];
		
		int result = is_class ? ModifyISS.getClassStackSize(target) :
			ModifyISS.getItemStackSize(target);
		
		if (quiet) return;
		String resultType = is_class ? "class name" : "item string ID";
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