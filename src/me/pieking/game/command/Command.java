package me.pieking.game.command;

import java.awt.event.KeyEvent;
import java.util.List;

import me.pieking.game.console.Console;
import me.pieking.game.events.KeyHandler;

public abstract class Command {

	public static void registerDefaultCommands(){
		registerCommand(new CommandHelp());
		registerCommand(new CommandReload());
		registerCommand(new CommandClear());
		registerCommand(new CommandPause());
		registerCommand(new CommandListFiles());
		registerCommand(new CommandDir());
		registerCommand(new CommandTxt());
		registerCommand(new CommandMkdir());
		registerCommand(new CommandMkfile());
		registerCommand(new CommandDel());
		registerCommand(new CommandCopy());
		registerCommand(new CommandMove());
		registerCommand(new CommandExit());
		registerCommand(new CommandRun());
		registerCommand(new CommandReboot());
		registerCommand(new CommandEdit());
		registerCommand(new CommandIpInfo());
		registerCommand(new CommandMessage());
		registerCommand(new CommandACE());
	}
	
	/**
	 * Register a command for the game.
	 * 
	 * @param com - The command object to use. This is an object of a class that extends the Command class.
	 * @return <b>true</b> if the command was not already registered.<br>
	 * <b>false</b> if the command was already registered.<br>
	 * (Note that if the command was already registered, the new one will override it.)
	 */
	public static boolean registerCommand(Command com){
		for(String l : com.label){
			KeyHandler.commands.put(l, com);
		}
		return true; //TODO? always successful
	}
	
	public final String[] label;
	public String usage = "No usage provided.";
	public String desc = "No description provided.";
	
	public boolean running = false;
	public boolean wantsInput = false;
	
	public Command(String... label){
		this.label = label;
	}
	
	public abstract boolean runCommand(Console console, List<String> args);
	
	public void type(KeyEvent ev){}

	public void write(String input) {}
	public void cancel() {}
	
}
