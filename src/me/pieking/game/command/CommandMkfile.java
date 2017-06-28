package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandMkfile extends Command{

	public CommandMkfile() {
		super("mkfile");
		
		desc  = "Create a file in the current location.";
		usage = "mkfile <folder_name>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if(!args.isEmpty()){
    		String currRelativePath = console.getDirectory();
    		File currentDir = new File(Game.getFileDir(), currRelativePath);
    		
    		String newRelativePath = args.get(0);
//    		if(!newRelativePath.startsWith("/")) newRelativePath = "/" + newRelativePath;
    		File newDir = new File(currentDir, newRelativePath);
    		
    		try{
        		if(newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())){
        			System.out.println(newDir + " " + newDir.exists() + " " + newDir.isDirectory());
            		if(newDir.exists()){
            			console.write("\\RA " + (newDir.isDirectory() ? "folder" : "file") + " with this name already exists.");
            			return false;
            		}else{
            			newDir.createNewFile();
    					console.write("Create " + newRelativePath);
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
