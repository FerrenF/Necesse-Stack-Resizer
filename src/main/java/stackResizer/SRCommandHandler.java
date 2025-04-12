package stackResizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.StringParameterHandler;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.commands.parameterHandlers.IntParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.gfx.GameColor;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.ItemCategory;
import necesse.inventory.item.ItemCategoryManager;
public class SRCommandHandler {

	public static SortedMap<String, ItemCategory> commandCategoryMapping = new TreeMap<String, ItemCategory>();
	
	public static Set<String> actions = new HashSet<String>();
	public static SortedSet<String> autoCompletes = new TreeSet<String>();	
	 
	public static void buildAutocompletes() {		
		
		ItemCategoryManager master = ItemCategory.masterManager;
		
		for( ItemCategory ic : master.masterCategory.getChildren() ) {		
			recurseChildren(ic);	
		}		
		actions.addAll(List.of("get","set","remove","blacklist","clear","default", "help", "info"));
		autoCompletes.addAll(commandCategoryMapping.keySet());
	
	}
	
	public static ItemCategory itemCategoryFromLastChild(String lastChild) {
		try {
			return commandCategoryMapping.get(lastChild);
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static void recurseChildren(ItemCategory ic) {
		String[] tree = ic.getStringIDTree(false);
		String lastTreeItem = tree[tree.length-1];
		if(!commandCategoryMapping.containsKey(lastTreeItem)) {
			commandCategoryMapping.put(lastTreeItem, ic);
		}	
		for( ItemCategory cc : ic.getChildren() ) {	
			recurseChildren(cc);
		}
	}

	public static String[] addHeldItem(String[] in) {			
		String[] out = new String[in.length+2];
		int index = 0;
		out[index] = "heldItem";
		index++;
		out[index] = "heldCategory";
		index++;
		for(String i : in) {
			out[index]=i;
			index++;
		}
		return out;
	}
	
	public static InventoryItem getHeldItem(ServerClient client) {
		return client.playerMob.getSelectedItem();
	}
	
	public static class StackSizeCommand extends ModularChatCommand {
		
		public StackSizeCommand() {
			super("stackresize", "Interact with Stack Resizer.", PermissionLevel.ADMIN, false,	
		new CmdParameter[]{
				new CmdParameter("action", new StringParameterHandler("set", actions.toArray(new String[] {}))),
				new CmdParameter("target",
						new StringParameterHandler("", addHeldItem(autoCompletes.toArray(new String[] {})))
						, true),
				new CmdParameter("amount", new IntParameterHandler(StackResizer.getDefaultStackSizeModifier()), true)
			});
		}

		public String getCurrentUsage(Client client, Server server, ServerClient serverClient, String[] args) {
			
			String action = args[0];
			switch(action) {
			
				case "info":
					return super.getCurrentUsage(client, server, serverClient, args)
							.replace(String.format(" [%starget%s] [%samount%s]", GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode(), GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode()),"")
							.replace(" [target] [amount]","")
							+ " - §9Return information about Stack Resizer.";
				case "get":
					return super.getCurrentUsage(client, server, serverClient, args)
							.replace(String.format(" [%starget%s]", GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode()),"")
							.replace(" [target]", "")
							+ " - §9Get an item or item category's current stack size. If target empty, get a list of all modifiers.";
				case "set":
					return super.getCurrentUsage(client, server, serverClient, args) + " - §9Set an item or item category's custom stack size.";
				case "remove":
					return super.getCurrentUsage(client, server, serverClient, args)
							.replace(String.format(" [%samount%s]", GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode()),"") 
							.replace(" [amount]","") 
							+ " - §9Remove an item or item category from BOTH the blacklist and custom modification list.";
				case "default":
					return super.getCurrentUsage(client, server, serverClient, args)
							.replace(String.format(" [%starget%s]", GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode()),"")
							.replace(" [target]", "") 
							+ " - §9Set the default stack size for all items. If value is empty, return the current default stack size.";
				case "blacklist":
					return super.getCurrentUsage(client, server, serverClient, args)
							.replace(String.format(" [%samount%s]", GameColor.YELLOW.getColorCode(),GameColor.NO_COLOR.getColorCode()),"")
							.replace(" [amount]","")
							+ " - §9Add an item or item category to the blacklist, and do not modify it.";
				case "clear":
					return "/stackresize clear - §9Clears all custom stack sizes and blacklists.";
			}
			return super.getCurrentUsage(client, server, serverClient, args);
		}
		
		public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors,
				CommandLog logs) {	
			
			  if (args.length < 1) {
	                logs.add("Invalid command usage. Type 'stackresize help' for command usage.");
	                return;
	            }
			
	            String action = (String) args[0];
	            String targetArg = args.length > 1 ? (String) args[1] : null;
	            Integer amount = args.length > 2 ? (Integer) args[2] : null;

	            SRSettings currentSettings = StackResizer.getCurrentSettings();
	            if (currentSettings == null) {
	                logs.add("Error: currentSettings is null.");
	                return;
	            }

	            switch (action) {
	                case "help":
	                    logs.add("Available commands: get, set, remove, blacklist, clear, default, help, info. Begin typing command for more information.");
	                    break;

	                case "info":
	                	LoadedMod _i = StackResizer.getCurrentModInstance();
	                	logs.add(_i.getModNameString());
	            		logs.add("Author: "+_i.author);
	                    logs.add("Stack Resizer modifies stack sizes dynamically.");
	                    logs.add("Settings stored at: "+StackResizer.getCurrentSettings().savePath());
	                    
	                    break;

	                case "clear":	 
	                	
	                   StackResizer.getCurrentSettings().itemBlacklist.clear();
	                   StackResizer.getCurrentSettings().categoryModifiers.clear();
	                   StackResizer.getCurrentSettings().itemModifiers.clear();
	                   StackResizer.getCurrentSettings().itemBlacklist.clear();
	                   logs.add("All items and categories cleared from blacklists and modifiers.");
	                   break;

	                case "default":
	                	
	                	if(targetArg == null || targetArg.isEmpty()) {
	                		logs.add("Default stack size modifier is currently " + StackResizer.getDefaultStackSizeModifier() + " for all items.");
	                		break;
	                	}
	                	logs.add("arg:" +targetArg);
	                	Integer amt = 0;
	                	try {
	                		amt = Integer.parseInt(targetArg);
	                	}catch(Exception e) {
	                		 logs.add("Error: 'default' requires an integer value as it's only parameter.");
	                		 break;
	                	}	                  
	                    StackResizer.setDefaultStackSizeModifier(amt);
	                    logs.add("Default stack size modifier set to: " + targetArg);
	                
	                    break;

	                default:
	                	
	                    String target = (String) targetArg;	                   
	                
	                    switch (action) {
	                    
	                        case "get":
	                        	
	                        	if(target.isBlank() || target.isEmpty()) {
	                        		StackResizer.getCategoryModifiers().forEach((_category, _amt)->{	                        			
	                        			logs.add("Category: " + _category.stringID + ": " + _amt);
	                        		});
	                        		
	                        		StackResizer.getItemModifiers().forEach((_item, _amt)->{	                        			
	                        			logs.add("Item: " + _item + ": " + _amt);
	                        		});
	                        	}
	                        	else {
	                        		
	                        		if(target.equals("heldItem")) {
		                        		target = getHeldItem(serverClient).item.getStringID();
		                        	}
		                        	else if(target.equals("heldCategory")) {		
		                        		String[] tree = ItemCategory.getItemsCategory(getHeldItem(serverClient).item).getStringIDTree(false);
		                        		target = tree[tree.length-1];
		                        	}
	                        	
		                        	if(commandCategoryMapping.containsKey(target) && StackResizer.getCategoryStackSize(commandCategoryMapping.get(target))!=-1){
		                        		logs.add("Stack size modifier for category " + target + " is " + StackResizer.getCategoryStackSize(commandCategoryMapping.get(target)));
		                        	}
		                        	else if(StackResizer.getItemStackSize(target)!=-1) {
	                            		logs.add("Stack size modifier for item " + target + " is " + StackResizer.getItemStackSize(targetArg) + ".");
	                            	}	      
		                        	else {
		                        		logs.add("No modifiers found for item " + target + ".");
		                        	}
	                        	}
	                            break;

	                        case "set":
	                        	
	                            if (amount == null) {
	                                logs.add("Error: 'set' requires an amount.");
	                            } else {
	                            	
	                            	if(target.equals("heldItem")) {
		                        		target = getHeldItem(serverClient).item.getStringID();
		                        	}
		                        	else if(target.equals("heldCategory")) {		
		                        		String[] tree = ItemCategory.getItemsCategory(getHeldItem(serverClient).item).getStringIDTree(false);
		                        		target = tree[tree.length-1];
		                        	}
	                            	
	                            	if(commandCategoryMapping.containsKey(target)){
	                            		StackResizer.setCategoryStackSizeModifier(commandCategoryMapping.get(target), amount);
	                            		logs.add("Set stack size modifier for category " + target + " to " + amount + ".");
	                            	}
	                            	else if(StackResizer.setItemStackSizeModifier(target, amount)!=-1) {
	                            		logs.add("Set stack size modifier for item " + target + " to " + amount + ".");
	                            	}
	                            	else {
	                            		logs.add("Could not find item or category named " + target + ".");
	                            	}
	                              
	                            }
	                            break;

	                        case "remove":
	                        	
	                        	if(target.equals("heldItem")) {
	                        		target = getHeldItem(serverClient).item.getStringID();
	                        	}
	                        	else if(target.equals("heldCategory")) {		
	                        		String[] tree = ItemCategory.getItemsCategory(getHeldItem(serverClient).item).getStringIDTree(false);
	                        		target = tree[tree.length-1];
	                        	}
	                        	
	                        	if(StackResizer.removeItemFromBlacklist(target)==-1) {
	      	                    	ItemCategory test = itemCategoryFromLastChild(target);
	      	                    	if(test!=null) {	      	                    		
	      	                    		if(StackResizer.removeItemCategoryFromBlacklist(test)!=-1) {
	      	                    			logs.add("Cleared category from blacklist.");
	      	                    		}	                    		
	      	                    	}
	      	                    }
	      	                    else {
	      	                    	logs.add("Cleared item from blacklist.");
	      	                    }
	      	                    
	      	                    if(StackResizer.removeItemStackSizeModifier(target)==-1) {	                    			
	      	                    	ItemCategory test = itemCategoryFromLastChild(target);
	      	                    	if(test!=null) {	                    		
	      	                    		if(StackResizer.removeItemCategoryFromModifiers(test)!=-1) {
	      	                    			logs.add("Cleared category from modifiers.");
	      	                    		}	                    		
	      	                    	}
	      	                    }
	      	                    else {
	      	                    	logs.add("Cleared item from modifiers.");
	      	                    }	  
	      	                    
	                            break;

	                        case "blacklist":
	                        	
	                        	if(commandCategoryMapping.containsKey((String)targetArg)){
                            		StackResizer.addCategoryToBlacklist(commandCategoryMapping.get(target));
                            		logs.add("Added category " + target + " to blacklist.");
                            	}
	                        	else if(StackResizer.addItemToBlacklist(target)!=-1){
	                        		StackResizer.addItemToBlacklist(target);
	                        		logs.add("Added " + target + " to blacklist.");
	                        	}	                        
	                          
	                            break;

	                        default:
	                            logs.add("Unknown action: " + action + ". Type 'stackresize help' for usage.");
	                            break;
	                    }
	            }
		}
	}
	

	
}
