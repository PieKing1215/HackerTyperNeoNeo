package me.pieking.game.events;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.pieking.game.command.Command;

public class KeyHandler implements KeyListener{

	private List<Integer> pressed = new ArrayList<Integer>();
	public Point lastMousePos;
	public boolean inInventory;
	
	public static boolean inCommandThing = false;
	
	public static String typing = "";
	public static long lastType = 0;
	public static int typeTime = 800;
	
	public static HashMap<String, Command> commands = new HashMap<String, Command>();
	
	public void keyJustPressed(KeyEvent e){
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
			typing = "";
			inCommandThing = false;
		}else if(e.getKeyCode() == KeyEvent.VK_ENTER){
			runCommand(typing);
			typing = "";
			inCommandThing = false;
		}
	}
	
	public static void runCommand(String cmd){
		String[] split = cmd.split(" ");
		String   label = split[0];
		String[] args  = new String[]{};
		try{
			args = cmd.substring(label.length() + 1).split(" ");
		}catch(Exception e1){}
		
		runCommand(label, new ArrayList<String>(Arrays.asList(args)));
	}
	
	public static void runCommand(String label, List<String> args) {

		Command toRun = commands.get(label);

		if (toRun != null) {
			toRun.runCommand(args);
		}
	}
	
	private boolean isNumber(char keyChar) {
		try{
			Integer.parseInt(keyChar + "");
			return true;
		}catch(Exception e){
			return false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!pressed.contains(e.getKeyCode())){
			pressed.add(e.getKeyCode());
			keyJustPressed(e);
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
    		if((int)e.getKeyChar() == KeyEvent.VK_BACK_SPACE /*http://stackoverflow.com/a/15693905*/){
    			if(typing.length() > 0){
    				typing = typing.substring(0, typing.length() - 1);
    				//if(inCommandThing) Sound.playSound(Sound.voiceGenericB, 0.5f);
    			}
    		}else if((int)e.getKeyChar() == KeyEvent.VK_ESCAPE /*http://stackoverflow.com/a/15693905*/){
    			typing = "";
    		}else if((int)e.getKeyChar() != KeyEvent.VK_ENTER && !pressed.contains(KeyEvent.VK_CONTROL)){
    			typing = typing + e.getKeyChar();
    			//if(inCommandThing) Sound.playSound(Sound.voiceGenericB, 0.5f);
    		}else if(pressed.contains(KeyEvent.VK_CONTROL)){
    			//System.out.println("aaaaaaaaa");
    			if((int)e.getKeyChar() == 22){
    				//System.out.println("ooooooo");
    				try {
    					String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor); 
						typing = typing + data;
						//if(inCommandThing) Sound.playSound(Sound.voiceGenericB, 0.5f);
					}
					catch (HeadlessException | UnsupportedFlavorException | IOException e1) {
						e1.printStackTrace();
					}
    			}
    		}
    	}else{
    		if((int)e.getKeyChar() != KeyEvent.VK_BACK_SPACE && (int)e.getKeyChar() != KeyEvent.VK_ESCAPE/*http://stackoverflow.com/a/15693905*/){
    			typing = "" + e.getKeyChar();
    			//if(inCommandThing) Sound.playSound(Sound.voiceGenericB, 0.5f);
    		}else{
    			typing = "";
    		}
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
