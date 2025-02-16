package main.java.ModifyISS.commands;

import main.java.ModifyISS.ModifyISS;
import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.ParsedCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;

public class TestExternalCommand extends ModularChatCommand {
    public TestExternalCommand() {
        super("stackresize.tests.external", "TEST: Modify and save the blacklist 'externally'.", PermissionLevel.OWNER, false, new CmdParameter[]{
            
        });
    }

    private void runServerCmd(Client client, String full_command) {
    	ServerClient myServerClient = client.getLocalServer().getLocalServerClient();
    	client.commandsManager.runServerCommand(new ParsedCommand(full_command), myServerClient);    	
    }
    
    private void getStackSizesExternally(Client client) {
    	if(client == null) return;
    	String commandString = "stackresize.stacksize";
    	runServerCmd(client, commandString);
    	return;
    }
    
    private void saveStackSizesExternally(Client client, boolean quiet) {
    	if(client == null) return;
    	String commandString = String.format("stackresize.save %s", String.valueOf(quiet));
    	runServerCmd(client, commandString);
    	return;
    }    

    private void setItemStackSizeExternallyWithCmdMgr(Client client, String item, int count,boolean is_class, boolean quiet) {
    	if(client == null) return;
    	String commandString = String.format("stackresize.stacksize.set %s %d %s %s", item, count, String.valueOf(is_class), String.valueOf(quiet));
    	runServerCmd(client, commandString);
    	return;
    }
    
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
                            CommandLog logs) {
    	
      	ModifyISS.ig_oops("Testing command manager access. Target: craftingguide, new stack size 5.");
        this.setItemStackSizeExternallyWithCmdMgr(server.getLocalClient(), "craftingguide", 5, false, false);
        
        ModifyISS.ig_oops("Attempting to print new lists to chat.");
        this.getStackSizesExternally(server.getLocalClient());
        
        ModifyISS.ig_oops("Attempting to save new list.");
        this.saveStackSizesExternally(server.getLocalClient(), false);
    }
}
