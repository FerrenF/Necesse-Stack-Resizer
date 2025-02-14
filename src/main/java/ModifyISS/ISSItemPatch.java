package main.java.ModifyISS;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.inventory.item.Item;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = Item.class, name = "getStackSize", arguments = {})
public class ISSItemPatch {
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter() {
        return true;
    }
    @Advice.OnMethodExit
    static void onExit(@Advice.This Item item, @Advice.FieldValue(value = "stackSize") int originalStackSize, @Advice.Return(readOnly = false) int stackSize) {
    	stackSize = ModifyISS.getStackSizeModification(item, originalStackSize);
    }
}
