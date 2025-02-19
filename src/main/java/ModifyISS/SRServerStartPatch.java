package main.java.ModifyISS;
import necesse.engine.modLoader.annotations.ModConstructorPatch;

import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerSettings;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = Server.class, arguments = {ServerSettings.class})
public class SRServerStartPatch {
    @Advice.OnMethodExit
    static void onExit(@Advice.This Server server,  @Advice.Argument(0) ServerSettings serverSettings) {
    	StackResizer.serverConnectEvent(server, serverSettings);
    }
}
