package stackResizer;
import java.awt.Color;

import necesse.engine.Settings;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.FairTypeTooltip;
import necesse.inventory.item.LocalMessageStringItemStatTip;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.gfx.gameTooltips.SpacerGameTooltip;
import net.bytebuddy.asm.Advice;


@ModMethodPatch(target = Item.class, name = "getTooltips", arguments = {InventoryItem.class, PlayerMob.class, GameBlackboard.class})
public class SRAddTooltipStacksizePatch {

	/*
	 * This patch adds the tool tip lines at the top of an item indicating the max stack size and if any filters are currently applied.
	 */
    @Advice.OnMethodExit
    static void onExit(	@Advice.This Item th, 
			    		@Advice.Argument(0) InventoryItem item, 
			    		@Advice.Argument(1) PlayerMob player,
			    		@Advice.Argument(2) GameBlackboard blackboard,
			    		@Advice.Return(readOnly = false) ListGameTooltips toolTipsList) {
    
    	toolTipsList.add(1, new SpacerGameTooltip(12));
    	
    	toolTipsList.add(1, new FairTypeTooltip(new LocalMessageStringItemStatTip("stackresizer", "tooltip_resized", "resize_state", StackResizer.getItemStackSizeStateString(th))
    			.toFairType((new FontOptions((int)(Settings.tooltipTextSize * 0.75))).outline(), Color.DARK_GRAY, Color.WHITE, Color.DARK_GRAY, false)));
    	
    	toolTipsList.add(1, new SpacerGameTooltip(1));
    	
    	toolTipsList.add(1, new FairTypeTooltip(new LocalMessageStringItemStatTip("stackresizer", "tooltip_stacksize", "value", String.valueOf(item.itemStackSize()))
    			.toFairType((new FontOptions((int)(Settings.tooltipTextSize * 0.75))).outline(), Color.DARK_GRAY, Color.WHITE, Color.DARK_GRAY, false)));
    		
    }
}
