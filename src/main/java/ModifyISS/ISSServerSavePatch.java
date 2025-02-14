package main.java.ModifyISS;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.Server;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = Server.class, name = "saveAll", arguments = {boolean.class})
public class ISSServerSavePatch {
    @Advice.OnMethodExit
    static void onExit(@Advice.This Server server,  @Advice.Argument(0) boolean fileSystemClose ) {
    	ModifyISS.serverSaveEvent(server);
    }
}
