package main.java.ModifyISS.commands;

import main.java.ModifyISS.ModifyISS;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

import necesse.engine.commands.parameterHandlers.BoolParameterHandler;

public class ReloadCommand extends ModularChatCommand {
	public ReloadCommand() {
		super("iss.reload", "Reloads the current blacklist and modified stack sizes from the mod's saved information.", PermissionLevel.OWNER, false, new CmdParameter[]{
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {				
		if(!(boolean)args[0]) logs.add("Loading ISS information...");
		ModifyISS.loadModData();
		logs.add("Completed.");
	}
}