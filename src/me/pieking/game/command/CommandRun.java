package me.pieking.game.command;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.console.Console;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.interpreter.Jasic;
import me.pieking.game.sound.Sound;

public class CommandRun extends Command{

	PipedInputStream in;
	PipedOutputStream out;
	AtomicBoolean cancel = new AtomicBoolean(false);
	
	public CommandRun() {
		super("run");
		
		desc  = "Run a Jasic (.jas) file.";
		usage = "run <relative_filename>";
		
		try {
			out = new PipedOutputStream();
			in = new PipedInputStream(out);
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean runCommand(Console console, List<String> args) {
		if(!args.isEmpty()){
    		String currRelativePath = console.getDirectory();
    		File currentDir = new File(Game.getFileDir(), currRelativePath);
    		
    		String newRelativePath = args.get(0);
    		File newDir = new File(currentDir, newRelativePath);
    		
    		boolean output = !args.contains("-noDisp");
    		
    		try{
        		if(newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())){
            		if(newDir.exists()){
        				if(newDir.getName().endsWith(".jas")){
        					new Thread(() -> {
        						if(output) console.write("\\YRunning " + newDir.getName() + "...");
        						cancel.set(false);
        						running = true;
        						try{
        							Jasic.runFile(newDir, console, in, cancel);
        						}catch(Exception e){
        							e.printStackTrace();
        							if(output) console.write("\\R" + e.getClass().getSimpleName() + ":" + e.getMessage());
        							running = false;
        						}
            					running = false;
            					if(cancel.get()){
            						if(output) console.write("\\RTerminated.");
            					}else{
            						if(output) console.write("\\GDone!");
            					}
            					wantsInput = false;
        					}).start();
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
	
	public static boolean canRun(String programName, Console console){
		String currRelativePath = console.getDirectory();
		File currentDir = new File(Game.getFileDir(), currRelativePath);
		
		String newRelativePath = programName;
		File newDir = new File(currentDir, newRelativePath);
		
		try{
    		if(newDir.getCanonicalPath().contains(Game.getFileDir().getCanonicalPath())){
        		if(newDir.exists()){
    				if(newDir.getName().endsWith(".jas")){
    					return true;
    				}
        		}
    		}
		}catch(IOException e){}
		return false;
	}
	
	@Override
	public void write(String input) {
		super.write(input);
		
		if(running) try {
			out.write((input + "\n").getBytes());
			out.flush();
			
//			System.out.println("avail = " + in.available());
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		cancel.set(true);
		
		try {
			in.close();
			out.close();
			out = new PipedOutputStream();
			in = new PipedInputStream(out);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
