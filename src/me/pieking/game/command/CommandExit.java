package me.pieking.game.command;

import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Logger.ExitState;
import me.pieking.game.console.Console;

public class CommandExit extends Command{

	public CommandExit() {
		super("exit");
		
		desc  = "Close the program.";
		usage = "exit";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		
		Game.stop(ExitState.OK.code);
		
		return true;
	}

}
