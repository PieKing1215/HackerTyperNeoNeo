package me.pieking.game.command;

import java.util.Collection;
import java.util.List;

import me.pieking.game.Logger;
import me.pieking.game.events.KeyHandler;

public class CommandHelp extends Command{

	public CommandHelp() {
		super("/help");
		
		desc  = "Get help with a command.";
		usage = "/help [command]";
	}

	@Override
	public boolean runCommand(List<String> args){
		if(!args.isEmpty()){
			String helpWith = args.get(0);
			
			if(!helpWith.startsWith("/")){
				helpWith = "/" + helpWith;
			}
			
			Command cmd = KeyHandler.commands.get(helpWith);
			
			if(cmd != null){
				Logger.info("[HELP] Help for " + cmd.label);
				Logger.info("[HELP] Description: \"" + cmd.desc + "\"");
				Logger.info("[HELP] Usage: \"" + cmd.usage + "\"");
			}else{
				Logger.info(" [HELP] Command not found: " + helpWith);
			}
		}else{
			Logger.info("[HELP] Command List:");
			Collection<Command> set = KeyHandler.commands.values();
			for(Command c : set){
				Logger.info("[HELP] " + c.label + " - " + c.desc);
			}
		}
		return true;
	}

}
