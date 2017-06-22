package me.pieking.game.command;

import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandReload extends Command{

	public CommandReload() {
		super("reload");
		
		desc  = "Reload parts of the game.";
		usage = "reload <textures|sounds|commands>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		boolean ret = false;
		if(!args.isEmpty()){
			String reload = args.get(0);
			
			ret = true;
			
			if(reload.equalsIgnoreCase("textures")){
				running = true;
				Sprite.reloadAll();
			}else if(reload.equalsIgnoreCase("sounds")){
				running = true;
				Sound.soundSystem.reload();
			}else if(reload.equalsIgnoreCase("commands")){
				running = true;
				Command.registerDefaultCommands();
			}else{
				console.write("\\RInvalid argument: '" + args.get(0) + "'");
				ret = false;
			}
		}else{
			console.write("\\RNot enough arguments.");
			ret = false;
		}
		
		running = false;
		return ret;
	}

}
