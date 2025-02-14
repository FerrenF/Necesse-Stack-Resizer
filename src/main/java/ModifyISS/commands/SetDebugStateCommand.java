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

public class SetDebugStateCommand extends ModularChatCommand {
	public SetDebugStateCommand() {
		super("iss.debug", "Sets the debug state for the mod. Can create log spam.", PermissionLevel.OWNER, false, new CmdParameter[]{
				new CmdParameter("value", new BoolParameterHandler(false),false)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {				
		logs.add((boolean)args[0] ? "Debug state is now on." : "Debug state is now off.");
		ModifyISS.setDebugState((boolean)args[0]);	
	}
}