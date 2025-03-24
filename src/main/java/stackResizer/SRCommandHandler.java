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

	public static class StackSizeCommand extends ModularChatCommand {
		
		public StackSizeCommand() {
			super("stackresize", "Interact with Stack Resizer.", PermissionLevel.ADMIN, false,	
		new CmdParameter[]{
				new CmdParameter("action", new StringParameterHandler("set", actions.toArray(new String[] {}))),
				new CmdParameter("target",
						new StringParameterHandler("", autoCompletes.toArray(new String[] {}))
						, true),
				new CmdParameter("amount", new IntParameterHandler(StackResizer.getDefaultStackSizeModifier()), true)
			});
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
	                        	
	                        	if(targetArg.isEmpty()) {
	                        		StackResizer.getCategoryModifiers().forEach((_category, _amt)->{	                        			
	                        			logs.add("Category: " + _category + ": " + _amt);
	                        		});
	                        	}
	                        	else if(commandCategoryMapping.containsKey(target)){
	                        		logs.add("Stack size modifier for category " + target + " is " + StackResizer.getCategoryStackSize(commandCategoryMapping.get(target)));
	                        	}
	                        	else if(StackResizer.getItemStackSize(target)!=-1) {
                            		logs.add("Stack size modifier for item " + target + " is " + amount + ".");
                            	}	      
	                        	else {
	                        		logs.add("No modifiers found for item " + target + ".");
	                        	}
	                            break;

	                        case "set":
	                        	
	                            if (amount == null) {
	                                logs.add("Error: 'set' requires an amount.");
	                            } else {
	                            	
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
