package me.pieking.game.command;

import java.util.List;

import me.pieking.game.events.KeyHandler;

public abstract class Command {

	public static void registerDefaultCommands(){
		registerCommand(new CommandHelp());
		registerCommand(new CommandReload());
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
		System.out.println(com.label + " " + com);
		return KeyHandler.commands.put(com.label, com) != null;
	}
	
	public final String label;
	public String usage = "No usage provided.";
	public String desc = "No description provided.";
	
	public Command(String label){
		this.label = label;
	}
	
	public abstract boolean runCommand(List<String> args);
	
}
