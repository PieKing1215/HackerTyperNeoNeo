package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.net.Client;
import me.pieking.game.sound.Sound;

public class CommandACE extends Command{

	public CommandACE() {
		super("ace");
		
		desc  = "Remote code execution";
		usage = "ace <ip> <file>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if(args.size() >= 2){
			String ip = args.get(0);
			
			String currRelativePath = console.getDirectory();
    		File currentDir = new File(Game.getFileDir(), currRelativePath);
    		
    		String newRelativePath = args.get(1);
    		File newDir = new File(currentDir, newRelativePath);
    		
    		boolean output = !args.contains("-noDisp");
    		
    		try{
        		if(newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())){
            		if(newDir.exists()){
        				if(newDir.getName().endsWith(".jas")){
        					String msg = Utils.readFile(newDir);
        					
        					try {
								msg = msg.replace("|", "\\|");
							}catch (Exception e) {
								if(output) console.write("\\RGZipException: " + e.getMessage());
								e.printStackTrace();
								return false;
							}
//        					System.out.println(args.size());
//        					
//        					System.out.println(msg);
        					
        					Client.write("ace|" + ip + "|" + msg);
        				}else{
        					if(output) console.write("\\R'" + newRelativePath + "' is not a .jas file.");
            			}
            		}else{
            			if(output) console.write("\\RThere is no file named '" + newRelativePath + "'.");
            			running = false;
            			return false;
            		}
        		}else{
        			if(output) console.write("\\RYou cannot access higher than root.");
    				return false;
        		}
    		}catch(IOException e){
    			if(output) console.write("\\RIOException: " + e.getMessage());
			}
			
		}else{
			console.write("\\RNot enough arguments.");
			return false;
		}
		return true;
	}

}
