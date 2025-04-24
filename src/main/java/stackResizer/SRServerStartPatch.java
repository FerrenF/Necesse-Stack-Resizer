package stackResizer;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerSettings;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = Server.class, arguments = {ServerSettings.class})
public class SRServerStartPatch {
	
	public static boolean started = false;
    @Advice.OnMethodExit
    static void onExit(@Advice.This Server server, @Advice.AllArguments Object[] args) {
		if(!started && server.world.filePath!=null) {
			StackResizer.worldStartEvent(server.world.filePath.getName(), server);
			started=true;
		}
    }
}
