package stackResizer;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.server.Server;
import necesse.engine.world.World;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = World.class, arguments = {Server.class})
public class SRServerStartPatch2 {
	
    @Advice.OnMethodExit
    static void onExit(@Advice.This World world,  @Advice.Argument(0) Server server) {
    	StackResizer.worldStartEvent(world);
    }
}
