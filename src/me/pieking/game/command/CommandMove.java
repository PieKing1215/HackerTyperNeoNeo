package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.console.Console;
import me.pieking.game.events.KeyHandler;

public class CommandMove extends Command {

	public CommandMove() {
		super("move", "mv");

		desc = "Move a file or folder to another location.";
		usage = "move <source_name> <dest_name>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if (args.size() >= 2) {
			String currRelativePath = console.getDirectory();
			File currentDir = new File(Game.getFileDir(), currRelativePath);

			String newRelativePath = args.get(0);
			File newDir = new File(currentDir, newRelativePath);
			
			String newRelativePath2 = args.get(1);
			File newDir2 = new File(currentDir, newRelativePath2);

			try {
				if (newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath()) && newDir2.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())) {
					System.out.println(newDir + " " + newDir.exists() + " " + newDir.isDirectory());
					if (newDir.exists()) {
						boolean overwrite = args.contains("-r");
						if(!newDir2.exists() || overwrite){
    						boolean success = true;
    						if(newDir.isDirectory()){
    							copyDirectory(newDir, newDir2, overwrite);
    //							success = deleteDirectory(newDir);
    						}else{
    							System.out.println("Copy " + newDir.getCanonicalPath() + " to " + newDir2.getCanonicalPath());
    							Files.copy(newDir.toPath(), newDir2.toPath());
    						}
    						
    						long fs1 = fileSize(newDir);
    						long fs2 = fileSize(newDir2);
    						
    						if(fs1 == fs2){
    							System.out.println("del " + newRelativePath);
    							console.runCommand("del " + newRelativePath);
    						}else{
    							console.write("\\RThe movement did not seem to complete, so the original file was not removed.");
    							return false;
    						}
    						
    						if(!success){
    							console.write("\\RCould not delete " + (newDir.isDirectory() ? "folder" : "file") + ".");
    							return false;
    						}
						}else{
							console.write("\\RA folder or file with this name exists.");
							console.write("\\RUse the '-r' argument to overwrite.");
							return false;
						}
					} else {
						console.write("\\RNo folder or file with this name exists.");
						return false;
					}
				} else {
					console.write("\\RYou cannot access higher than root.");
					return false;
				}
			}
			catch (IOException e) {
				console.write("\\RIOException: " + e.getMessage());
			}
		} else {
			console.write("\\RNot enough arguments.");
			return false;
		}

		return true;
	}

	public static void copyDirectory(File source, File dest, boolean overwrite) throws IOException {
		if(dest.exists() && !overwrite) return;
		
		if(dest.exists() && overwrite){
			if(!deleteDirectory(dest)) return;
		}
		
		if(!dest.mkdirs()) return;
		
		File[] children = source.listFiles();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			String relPath = child.getCanonicalPath().replace(source.getCanonicalPath(), "");
			File childDest = new File(dest, relPath);
			
			System.out.println(child.getCanonicalPath() + " " + childDest.getCanonicalPath());
			
			if(child.isDirectory()){
				copyDirectory(child, childDest, overwrite);
			}else{
				Files.copy(child.toPath(), childDest.toPath());
			}
		}
		
	}
	
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
			return dir.delete();
		}else{
			return dir.delete();
		}
	}

	public static long fileSize(File file){
		if(file.isDirectory()) return folderSize(file);
		return file.length();
	}
	
	public static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
	
}
