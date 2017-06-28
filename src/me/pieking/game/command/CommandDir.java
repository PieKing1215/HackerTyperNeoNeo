package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.console.Console;

public class CommandDir extends Command{

	public CommandDir() {
		super("dir");
		
		desc  = "Change directory.";
		usage = "dir <relative_directory>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {

		if(!args.isEmpty()){
    		String currRelativePath = console.getDirectory();
    		File currentDir = new File(Game.getFileDir(), currRelativePath);

			String newRelativePath = args.get(0);
			File newDir = new File(currentDir, newRelativePath);
			try {
				if (newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())) {
					if (newDir.exists()) {
						console.changeDirectory(newRelativePath);
					} else {
						console.write("\\RThere is no directory named '" + newRelativePath + "'.");
						return false;
					}
				} else {
					console.write("\\RYou cannot navigate higher than root.");
					return false;
				}
			}catch (IOException e) {
				console.write("\\RIOException: " + e.getMessage());
			}

//			try {
//				System.out.println(newDir.getCanonicalPath() + " " + newDir.exists());
//			}
//			catch (IOException e) {
//				e.printStackTrace();
//			}
		}else{
			console.write("\\RNot enough arguments.");
			return false;
		}
		
		return true;
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
	
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
