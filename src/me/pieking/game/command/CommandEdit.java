package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Scheduler;
import me.pieking.game.Utils;
import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandEdit extends Command{

	public CommandEdit() {
		super("edit");
		
		desc  = "Edit a file.";
		usage = "edit <relative_filename>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if(!args.isEmpty()){
    		String currRelativePath = console.getDirectory();
    		File currentDir = new File(Game.getFileDir(), currRelativePath);
    		
    		String newRelativePath = args.get(0);
    		File newDir = new File(currentDir, newRelativePath);
    		
    		try{
        		if(newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())){
            		if(newDir.exists()){
    					if(newDir.isFile()){
    						running = true;
    						console.write("\\YLoading '" + newRelativePath + "'...");
    						Scheduler.delayedTask(() -> {
    							Game.edit(newRelativePath);
    							running = false;
    						}, 60);
    					}else{
    						console.write("\\R'" + newRelativePath + "' is not a file.");
                			return false;
    					}
            		}else{
            			console.write("\\RThere is no file named '" + newRelativePath + "'.");
            			return false;
            		}
        		}else{
    				console.write("\\RYou cannot access higher than root.");
    				return false;
        		}
    		}catch(IOException e){
				console.write("\\RIOException: " + e.getMessage());
			}
    		
//    		try {
//				System.out.println(newDir.getCanonicalPath() + " " + newDir.exists());
//			}catch (IOException e) {
//				e.printStackTrace();
//			}
		}else{
			console.write("\\RNot enough arguments.");
			return false;
		}
		
		return true;
	}

}
