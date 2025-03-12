package stackResizer.commands;

import stackResizer.StackResizer;

import java.util.List;

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
			int c_s = StackResizer.getDefaultStackSizeModifier();	
			logs.add("Default stack size: "+c_s);
			logs.add("Class modifiers: ");
			List<String> classlist = StackResizer.getCurrentSettings().classModifiers.keySet().stream().map((classname)->classname.getName()).toList();
			classlist.forEach((msg)->logs.add("CLASS: "+msg));
			logs.add("Item modifiers: ");
			List<String> itemlist = StackResizer.getCurrentSettings().itemModifiers.entrySet().stream().map((entry)->String.format("%s - %s", entry.getKey(), entry.getValue())).toList();
			itemlist.forEach((msg)->logs.add("ITEM: "+msg));
	}
}