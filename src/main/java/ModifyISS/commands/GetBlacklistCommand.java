package main.java.ModifyISS.commands;

import main.java.ModifyISS.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class GetBlacklistCommand extends ModularChatCommand {
	public GetBlacklistCommand() {
		super("stackresize.blacklist", "Get the current blacklists.", PermissionLevel.USER, false,new CmdParameter[]{});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {
		
		logs.add("Current blacklists:");
		logs.add("CLASS: "+StackResizer.getCurrentSettings().classBlacklistToString());
		logs.add("ITEM: "+StackResizer.getCurrentSettings().itemBlacklistToString());
	}
}