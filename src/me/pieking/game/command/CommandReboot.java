package me.pieking.game.command;

import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.console.Console;

public class CommandReboot extends Command{

	public CommandReboot() {
		super("reboot");
		
		desc  = "Reboot the program.";
		usage = "reboot";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		Game.reboot();
		return true;
	}

}
