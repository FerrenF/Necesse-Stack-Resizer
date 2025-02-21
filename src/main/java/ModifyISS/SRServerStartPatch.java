package main.java.ModifyISS;
import java.io.File;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.world.World;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = World.class, arguments = {File.class, boolean.class})
public class SRServerStartPatch {
	
    @Advice.OnMethodExit
    static void onExit(@Advice.This World world,  @Advice.Argument(0) File file, @Advice.Argument(1) boolean isSimple) {
    	StackResizer.worldStartEvent(world);
    }
}
