package main.java.ModifyISS.commands;

import main.java.ModifyISS.StackResizer;
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
		super("stackresize.enabled", "Sets the enabled state of the mod. Unstable feature.", PermissionLevel.OWNER, false, new CmdParameter[]{
				new CmdParameter("value", new BoolParameterHandler(true),false),
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {				
		boolean result_state = (boolean)args[0];
		boolean quiet = (boolean) args[1];
		
		
		StackResizer.setEnabled(result_state);	
		
		if(quiet) return;
		logs.add(result_state ? "Enabled state is true." : "Enabled state is false.");
	}
}