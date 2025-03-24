package stackResizer;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.Server;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = Server.class, name = "saveAll", arguments = {boolean.class})
public class SRServerSavePatch {
    @Advice.OnMethodExit
    static void onExit(@Advice.This Server th, @Advice.AllArguments Object[] args) {
    	StackResizer.dbg_oops("saveAll intercepted");
    	StackResizer.serverSaveEvent();
    }
}
