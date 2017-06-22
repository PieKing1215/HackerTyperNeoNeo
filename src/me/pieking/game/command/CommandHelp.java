package me.pieking.game.command;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import me.pieking.game.console.Console;
import me.pieking.game.events.KeyHandler;

public class CommandHelp extends Command{

	public CommandHelp() {
		super("help", "?");
		
		desc  = "Get help with a command.";
		usage = "help [command]";
	}

	@Override
	public boolean runCommand(Console console, List<String> args){
		if(!args.isEmpty()){
			String helpWith = args.get(0);
			
			Command cmd = KeyHandler.commands.get(helpWith);
			
			if(cmd != null){
				console.write("\\YHelp for " + cmd.label[0] + ":");
				console.write("Description: \"" + cmd.desc + "\"");
				console.write("Usage: \"" + cmd.usage + "\"");
				
				String aliases = "";
				for(int i = 0; i < cmd.label.length; i++){
					if(i > 0){
						String s = cmd.label[i];
						aliases += "\"" + s + "\" ";
					}
				}
				
				if(!aliases.isEmpty()) console.write("Aliases: " + aliases + "");
			}else{
				console.write("\\RCommand not found: " + helpWith);
				return false;
			}
		}else{
			console.write("\\YCommand List:");
			List<Command> commands = new ArrayList<Command>();
			commands.addAll(KeyHandler.commands.values());
			Set<Command> hs = new LinkedHashSet<Command>(commands);
			commands.clear();
			commands.addAll(hs);
			
			for(Command c : commands){
				console.write("" + c.label[0] + " - " + c.desc);
			}
		}
		return true;
	}

}
