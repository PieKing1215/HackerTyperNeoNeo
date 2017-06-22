package me.pieking.game.command;

import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandClear extends Command{

	public CommandClear() {
		super("cls");
		
		desc  = "Clear the screen";
		usage = "cls";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		console.clear();
		return true;
	}

}
