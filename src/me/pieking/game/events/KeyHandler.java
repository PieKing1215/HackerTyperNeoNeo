package me.pieking.game.events;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.command.Command;
import me.pieking.game.console.Console;

public class KeyHandler implements KeyListener{

	private List<Integer> pressed = new ArrayList<Integer>();
	public Point lastMousePos;
	public boolean inInventory;
	
	public static final boolean inCommandThing = true;
	
	public static long lastType = 0;
	public static int typeTime = 800;
	
	public static LinkedHashMap<String, Command> commands = new LinkedHashMap<String, Command>();
	
	public void keyJustPressed(KeyEvent e){
		
	}
	
	public static Command getCommand(String label){
		return commands.get(label);
	}
	
	public static void runCommand(String cmd){
		runCommand(Game.getMainConsole(), cmd);
	}
	
	public static void runCommand(Console console, String cmd){
		String[] split = cmd.split(" ");
		String   label = split[0];
		String[] args  = new String[]{};
		try{
			args = cmd.substring(label.length() + 1).split(" ");
		}catch(Exception e1){}
		
		runCommand(console, label, new ArrayList<String>(Arrays.asList(args)));
	}
	
	public static void runCommand(String label, List<String> args) {
		runCommand(Game.getMainConsole(), label, args);
	}
	
	public static void runCommand(Console console, String label, List<String> args) {
		Command toRun = commands.get(label);

		if (toRun != null) {
			toRun.runCommand(console, args);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!pressed.contains(e.getKeyCode())){
			pressed.add(e.getKeyCode());
			keyJustPressed(e);
		}
		
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			Game.getFocusedArea().left();
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			Game.getFocusedArea().right();
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(pressed.contains(e.getKeyCode())){
			pressed.remove((Object)e.getKeyCode()); //cast the code to Object so it uses remove(Object) instead of remove(int)
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		long now = System.currentTimeMillis();
    	if(now - lastType < typeTime || inCommandThing){
    		Game.getFocusedArea().type(e);
    	}
    	lastType = now;
	}

	public List<Integer> getPressed(){
		List<Integer> ret = new ArrayList<Integer>();
		ret.addAll(pressed);
		return ret;
	}
	
	public boolean isPressed(int keyCode){
		return (pressed.contains(keyCode));
	}
	
	public boolean isPressed(char keyChar){
		return isPressed(KeyEvent.getExtendedKeyCodeForChar(keyChar));
	}
	
}
