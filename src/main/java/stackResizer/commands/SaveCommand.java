package stackResizer.commands;

import stackResizer.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

import necesse.engine.commands.parameterHandlers.BoolParameterHandler;

public class SaveCommand extends ModularChatCommand {
	public SaveCommand() {
		super("stackresize.save", "Saves the current blacklist and modified stack sizes.", PermissionLevel.OWNER, false, new CmdParameter[]{
				new CmdParameter("quiet", new BoolParameterHandler(false),true)
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {				
		if(!(boolean)args[0]) logs.add("Saving ISS information.");
		StackResizer.saveModData();	
	}
}