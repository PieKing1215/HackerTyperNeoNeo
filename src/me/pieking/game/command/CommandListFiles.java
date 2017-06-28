package me.pieking.game.command;

import java.io.File;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.console.Console;

public class CommandListFiles extends Command{

	public CommandListFiles() {
		super("listfiles", "lf");
		
		desc  = "Lists the files in this directory.";
		usage = "listfiles";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		
		String relativePath = console.getDirectory();
		File realDir = new File(Game.getFileDir(), relativePath);
		
		String dispPath = relativePath.replace("\\", "/");
		
		if(dispPath.isEmpty()) dispPath = "/";
		
		console.write("\\YFiles in '" + dispPath +"':");
		File[] files = realDir.listFiles();
		
		int maxSize = 0;
		
		for(File f : files){
			maxSize = Math.max(maxSize, f.getName().length());
		}
		
		maxSize += 2;
		
		for(File f : files){
			if(f.isDirectory()){
				String name = f.getName();
				String padding = new String(new char[maxSize - (name.length()+1)]).replace("\0", " ");
				System.out.println(padding);
				console.write("/" + f.getName() + padding + humanReadableByteCount(folderSize(f), true));
			}
		}
		
		for(File f : files){
			if(!f.isDirectory()){
				String name = f.getName();
				String padding = new String(new char[maxSize - name.length()]).replace("\0", " ");
				console.write(f.getName() + padding + humanReadableByteCount(f.length(), true));
			}
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
