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

public class SetEnabledStateCommand extends ModularChatCommand {
	public SetEnabledStateCommand() {
		super("iss.enabled", "Sets the enabled state of the mod. Unstable feature.", PermissionLevel.OWNER, false, new CmdParameter[]{
				new CmdParameter("value", new BoolParameterHandler(true),false)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {				
		logs.add((boolean)args[0] ? "Enabled state is true." : "Enabled state is false.");
		ModifyISS.setEnabled((boolean)args[0]);	
	}
}