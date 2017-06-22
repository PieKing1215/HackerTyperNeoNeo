package me.pieking.game.command;

import java.awt.event.KeyEvent;
import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandPause extends Command{

	public CommandPause() {
		super("pause");
		
		desc  = "Pause until input.";
		usage = "pause [timeout_in_ms]";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		boolean ret = true;
		
		if(args.size() >= 1){
			try {
				int delay = Integer.parseInt(args.get(0));
			}catch(NumberFormatException e){
				console.write("\\RInvalid number: " + args.get(0));
				ret = false;
			}
		}
		
		if(ret){
			console.write("Press any key to continue...");
			running = true;
		}
		return ret;
	}
	
	@Override
	public void type(KeyEvent ev) {
		running = false;
		System.out.println("type" + ev.getKeyChar());
	}

}
