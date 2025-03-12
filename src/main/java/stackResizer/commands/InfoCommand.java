package stackResizer.commands;

import stackResizer.StackResizer;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class InfoCommand extends ModularChatCommand {
	public InfoCommand() {
		super("stackresize", "Returns mod information.", PermissionLevel.USER, false, new CmdParameter[]{
		});
	}

	public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
			CommandLog logs) {		
		LoadedMod _i = StackResizer.getCurrentModInstance();
		logs.add(_i.getModNameString());
		logs.add("Author: "+_i.author);
		logs.add("Debug State:" + String.valueOf(StackResizer.getDebugState()));
		logs.add("ISS Enabled: " + String.valueOf(StackResizer.getEnabled()));
		if(client == null || (client.getPermissionLevel() == PermissionLevel.OWNER || client.getPermissionLevel() == PermissionLevel.SERVER)) {
			logs.add("World settings at: " + StackResizer.getCurrentSettings().savePath());
		}
		
	}
}