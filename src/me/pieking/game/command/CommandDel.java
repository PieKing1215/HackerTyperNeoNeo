package me.pieking.game.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandDel extends Command {

	public CommandDel() {
		super("del");

		desc = "Delete a file or folder in the current location.";
		usage = "del <folder_or_file_name>";
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if (!args.isEmpty()) {
			String currRelativePath = console.getDirectory();
			File currentDir = new File(Game.getFileDir(), currRelativePath);

			String newRelativePath = args.get(0);
			File newDir = new File(currentDir, newRelativePath);

			try {
				if (newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())) {
					System.out.println(newDir + " " + newDir.exists() + " " + newDir.isDirectory());
					if (newDir.exists()) {
						boolean success = true;
						if(newDir.isDirectory()){
							success = deleteDirectory(newDir);
						}else{
							success = newDir.delete();
						}
						
						if(currentDir.getCanonicalPath().contains(newDir.getCanonicalPath())){
							console.setDirectory(newDir.getCanonicalPath().replace(currentDir.getCanonicalPath(), "").replace(Game.getFileDir().getCanonicalPath(), ""));
							System.out.println("up to" + newDir.getCanonicalPath().replace(currentDir.getCanonicalPath(), ""));
						}
						
						if(!success){
							console.write("\\RCould not delete " + (newDir.isDirectory() ? "folder" : "file") + ".");
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

	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
			System.out.println("deleting " + dir + " with " + dir.listFiles().length + " children.");
			try{
				Files.delete(dir.toPath());
				return true;
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
		}else{
			return dir.delete();
		}
	}

}
