package main.java.ModifyISS.commands;

import main.java.ModifyISS.ModifyISS;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class StackSizeCommand extends ModularChatCommand {
	public StackSizeCommand() {
		super("stackresize.stacksize", "Return information about the current default stack size and modifier lists.", PermissionLevel.USER, false, new CmdParameter[]{
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {	
			int c_s = ModifyISS.getDefaultStackSizeModifier();	
			logs.add("Default stack size: "+c_s);
			logs.add("Class modifiers: "+ModifyISS.getCurrentSettings().classModifierListToString());
			logs.add("Item modifiers: "+ModifyISS.getCurrentSettings().itemModifierListToString());
	
	}
}