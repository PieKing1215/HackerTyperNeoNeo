package me.pieking.game.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Rand;
import me.pieking.game.Scheduler;
import me.pieking.game.command.Command;
import me.pieking.game.command.CommandListFiles;
import me.pieking.game.command.CommandRun;
import me.pieking.game.events.KeyHandler;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.FormattedString;

public class Console {
	
	public int charPerLine = 60;
	private List<FormattedString> lines = new ArrayList<FormattedString>();
	private int maxLines = 5;
	
	private int scrollOfs = 0;
	
	private Command runningCommand;
	
	public String typing = "";
	public int cursorIndex = typing.length();
	
	public int blinkTimer = 0;
	public int fontSize = 20;
	private int maxScroll;
	
	public int inputDelay = 0;
	
	public String prefix = "> ";
	
	public File currDir;
	public boolean canInput = false;
	
	public Console() {
		setDirectory("");
	}
	
	public void changeDirectory(String relativeChange){
		File newDir = new File(currDir, relativeChange);
		newDir.mkdir();
		if(newDir.isDirectory()){
			currDir = newDir;
		}
	}
	
	public void setDirectory(String str){
		File newDir = new File(Game.getFileDir(), str);
		newDir.mkdir();
		if(newDir.isDirectory()){
			currDir = newDir;
		}
	}
	
	public String getDirectory(){
		try{
			return currDir.getCanonicalPath().replace(Game.getFileDir().getAbsolutePath(), "");
		}catch(IOException e){
			e.printStackTrace();
		}
		return currDir.getAbsolutePath().replace(Game.getFileDir().getAbsolutePath(), "");
	}
	
	public void tick(){
		if(scrollOfs < 0 && lines.isEmpty()) scrollOfs = 0;
		blinkTimer++;
		fontSize = 14;
		maxLines = 128;
		charPerLine = 78;
		if(inputDelay > 0) inputDelay--;
		String dir = getDirectory().replace("\\", "/");
		if(!dir.startsWith("/")) dir = "/" + dir;
		if(runningCommand != null && runningCommand.wantsInput){
			prefix = "? ";
		}else{
			prefix = "" + (currDir == null ? "" : dir) + "> ";
		}
	}
	
	public void render(Graphics2D g){
		List<FormattedString> str = new ArrayList<FormattedString>();
		str.addAll(lines);
		int lastOfs = 0;
		for(int i = 0; i < str.size(); i++){
			FormattedString line = str.get(i);
//			line.setRawString(line.getRawString().replaceAll("\\\\.", ""));
			
			g.setFont(Fonts.anonymous.deriveFont((float)fontSize));
			
			int h = height(line, g.getFont());
			
			int y = fontSize*2 + lastOfs + scrollOfs;
			
			if(y > 0 && y - fontSize < Game.getHeight()) line.renderFormatted(g, fontSize, fontSize*2 + lastOfs + scrollOfs);
			
			lastOfs += h;
		}
		
		if(awaitingInput()){
			FormattedString line = new FormattedString(prefix + typing);
			
			g.setFont(Fonts.anonymous.deriveFont((float)fontSize));
			
			line.renderRaw(g, fontSize, fontSize*2 + lastOfs + scrollOfs);
			
			int fontW = g.getFontMetrics().stringWidth("w")+1;
			
			String broken = "";
			try{
				broken = FormattedString.addLinebreaks(prefix + typing, charPerLine).replace(" \n", "\n").substring(prefix.length(), cursorIndex+prefix.length());
			}catch(StringIndexOutOfBoundsException e){}
			
			int totalBreaks = broken.length() - broken.replace("\n", "").length();
			int lastBreak = broken.lastIndexOf("\n");
			int yOfs = lastOfs + (int) (totalBreaks * (g.getFont().getSize() * 1.15));
			
//			System.out.println();
//			System.out.println(broken);
//			System.out.println(totalBreaks);
//			System.out.println(lastBreak);
//			System.out.println(cursorIndex);
			
			if(blinkTimer % 60 < 30){
				int ofs = prefix.length()-1;
				if(totalBreaks > 0) ofs = -1;
				if(cursorIndex == typing.length()){
					g.fillRect(fontSize + (cursorIndex+ofs - lastBreak)*fontW, fontSize*2 + scrollOfs + yOfs, fontW, 2);
				}else{
					g.fillRect(fontSize + (cursorIndex+ofs - lastBreak)*fontW - 2, 2 + scrollOfs + yOfs + fontSize, 2, fontSize);
				}
			}
		}
		
		int minScroll = -(maxScroll-Game.getHeight() + fontSize*4);
		int maxScroll = 0;

		int dif = maxScroll - minScroll;
		float thru = -scrollOfs/(float)dif;
		
//		System.out.println();
//		System.out.println(minScroll);
//		System.out.println(scrollOfs);
//		System.out.println(maxScroll);
//		System.out.println(thru);
		
		if(this.maxScroll >= Game.getHeight()-fontSize*3){
    		int size = Math.max(minScroll + 600, 48);
    		
//    		System.out.println(size);
    		
    		g.setColor(Color.DARK_GRAY);
    		g.fillRect(Game.getWidth()-8, 0, 8, Game.getHeight());
    		
    		int ofs = (int) (thru * (Game.getHeight()-size));
    		g.setColor(Color.GRAY);
    		g.fill3DRect(Game.getWidth()-8, ofs, 8, size, true);
		}
		
		if(runningCommand != null && runningCommand.running){
    		g.setColor(Color.WHITE);
    		int runOfs = 80-(Game.getTime()%80) - 44;
    		Shape s = g.getClip();
    		g.setClip(Game.getWidth()-20, 0, 10, 30);
    		g.fillRect(Game.getWidth()-17, runOfs, 4, 40);
    		g.setClip(s);
		}
		//System.out.println(scrollOfs);
		
	}
	
	public List<FormattedString> getLines(){
		return lines;
	}
	
	public void write(String s){
		write(new FormattedString(s + "\\W "));
	}
	
	public void write(FormattedString s){
		lines.add(s);
		
		while(lines.size() > maxLines){
			lines.remove(0);
		}
		
		//System.out.println(fontSize + (fontSize * maxLines) + scrollOfs);
		
//		if(fontSize + (fontSize * lines.size()) + scrollOfs > Game.getHeight()-fontSize){
//			scrollOfs -= fontSize;
//		}
		
		List<FormattedString> str = new ArrayList<FormattedString>();
		str.addAll(lines);
		int lastOfs = 0;
		
		for(int i = 0; i < str.size(); i++){
			FormattedString line = str.get(i);
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize*2 + lastOfs + scrollOfs;
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight()+fontSize;
				scrollOfs -= diff;
				y -= diff;
			}
			lastOfs += h;
		}
		
		if(awaitingInput()){
			FormattedString line = new FormattedString(prefix + typing);
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize*2 + lastOfs + scrollOfs;
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight()+fontSize;
				scrollOfs -= diff;
				y -= diff;
			}
		}
		
	}
	
	public void setMaxLines(int lines){
		this.maxLines = lines;
	}
	
	public int getMaxLines(){
		return this.maxLines;
	}
	
	public boolean awaitingInput(){
		if(!canInput ) return false;
		if(runningCommand == null) return true;
		if(runningCommand.wantsInput) return true;
		return !runningCommand.running;
	}

	public void enter() {

		inputDelay = 10;
		
		if(typing.isEmpty()) return;
		
		cursorIndex = 0;
		String cmd = typing;
		typing = "";
		
		if(runningCommand != null && runningCommand.wantsInput){
			System.out.println("running in " + cmd);
			runningCommand.write(cmd);
		}else{
    		write(prefix + cmd);
    		runCommand(cmd);
		}
		
		scroll(1);
	}
	
	public void runCommand(String cmd){
		runCommand(cmd, true);
	}
	
	public void runCommand(String cmd, boolean setRunning){
		String[] split = cmd.split(" ");
		String   label = split[0];
		String[] args  = new String[]{};
		try{
			args = cmd.substring(label.length() + 1).split(" ");
		}catch(Exception e1){}
		
		Command toRun = KeyHandler.commands.get(label);

		if(setRunning) runningCommand = toRun;
		
		if (toRun != null) {
			boolean success = toRun.runCommand(this, new ArrayList<String>(Arrays.asList(args)));
			if(!success){
				write("Usage: \"" + toRun.usage + "\"");
			}
		}else{
			String prog = label;
			if(!prog.endsWith(".jas")) prog += ".jas";
			if(CommandRun.canRun(prog, this)){
				runCommand("run " + prog, setRunning);
			}else{
				write("\\R'" + label + "\\R' is not a recognized command or program.");
			}
		}
	}

	public void left() {
		blinkTimer = 0;
		
		if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
			if(cursorIndex > 0){
    			String sub = typing.substring(0, cursorIndex-1);
    			int space = sub.lastIndexOf(" ");
    			
    			cursorIndex = Math.max(space+1, 0);
			}else{
				cursorIndex = 0;
			}
		}else{
			if(cursorIndex > 0) cursorIndex--;
		}
	}

	public void right() {
		blinkTimer = 0;
		
		if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
			
			if(cursorIndex < typing.length()){
    			String sub = typing.substring(cursorIndex);
    			int space = sub.indexOf(" ");
    			
//    			System.out.println(space);
    			
    			if(space != -1){
    				cursorIndex += space+1;
    			}else{
    				cursorIndex = typing.length();
    			}
			}else{
				cursorIndex = typing.length();
			}
		}else{
			if(cursorIndex < typing.length()) cursorIndex++;
		}
	}

	public void type(KeyEvent e) {
		
//		System.out.println(inputDelay + " " + awaitingInput());
		if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL) && Game.keyHandler().isPressed(KeyEvent.VK_T)){
			if(runningCommand != null){
				if(runningCommand.running) {
					runningCommand.cancel();
					runningCommand = null;
				}
			}
		}
		
		if(inputDelay > 0) return;
		
		if(!awaitingInput()){
			if(runningCommand != null) runningCommand.type(e);
			return;
		}
		
		StringBuilder sb = new StringBuilder(typing);
		
		if((int)e.getKeyChar() == KeyEvent.VK_BACK_SPACE /* http://stackoverflow.com/a/15693905 */){
			if(sb.length() > 0){
				if(cursorIndex > 0) {
					sb.deleteCharAt(cursorIndex-1);
					left();
				}
			}
		}else if((int)e.getKeyChar() == KeyEvent.VK_DELETE /* http://stackoverflow.com/a/15693905 */){
			if(sb.length() > 0){
				if(cursorIndex < sb.length()) {
					sb.deleteCharAt(cursorIndex);
				}
			}
		}else if((int)e.getKeyChar() == KeyEvent.VK_ESCAPE /* http://stackoverflow.com/a/15693905 */){
			sb = new StringBuilder("");
			cursorIndex = 0;
		}else if((int)e.getKeyChar() != KeyEvent.VK_ENTER && !Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
			sb.insert(cursorIndex, e.getKeyChar());
			cursorIndex++;
		}else if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
			if((int)e.getKeyChar() == 22){
				try {
					String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor); 
					sb.insert(cursorIndex, data);
					cursorIndex += data.length();
				} catch (HeadlessException | UnsupportedFlavorException | IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		blinkTimer = 0;
		typing = sb.toString();
		scroll(maxLines);
	}

	public void clear() {
		lines.clear();
		scrollOfs = 0;
		maxScroll = 0;
	}
	
	public int height(FormattedString str, Font f){
		float lineSize = f.getSize() * 1.15f;
		try{
    		return (int) (str.broken_f.split("\n").length * lineSize);
		}catch(Exception e){
			return (int) lineSize;
		}
	}

	public void scroll(MouseWheelEvent e) {
		scroll(e.getWheelRotation() * 2);
	}
	
	public void scroll(int scrollAmount) {
		int newScroll = (int) (scrollOfs - scrollAmount * (fontSize * 1.15f));
	
		if(scrollAmount < 0) newScroll--;
		
//		System.out.println(newScroll + " " + maxScroll);

		List<FormattedString> str = new ArrayList<FormattedString>();
		str.addAll(lines);
		maxScroll = 0;
		
		for(int i = 0; i < str.size(); i++){
			FormattedString line = str.get(i);
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize  + maxScroll + newScroll;
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight();
				//newScroll -= diff;
				y -= diff;
			}
			maxScroll += h;
		}
		
		if(awaitingInput()){
			FormattedString line = new FormattedString(prefix + typing);
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize + maxScroll + newScroll;
//			System.out.println(y + " " + h + " " + newScroll);
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight();
				//newScroll -= diff;
				y -= diff;
			}
		}
		
//		System.out.println(maxScroll + " " + newScroll);
		
		if(scrollAmount > 0 && newScroll < -(maxScroll-Game.getHeight() + fontSize*4)) newScroll = -(maxScroll-Game.getHeight() + fontSize*4); //TODO: figure out how to calculate this magic number 
		
//		System.out.println(newScroll);
		if(newScroll > 0) newScroll = 0;
		
		if(maxScroll < Game.getHeight()-fontSize*3) newScroll = 0;
		
//		System.out.println(maxScroll);
		
		scrollOfs = newScroll;
	}
	
	public Command getRunning(){
		return runningCommand;
	}

	public void startup() {
		
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		
		List<String> bootSequence = new ArrayList<String>();
		bootSequence.add("Booting...");
		bootSequence.add("OS = " + osName + "\nOS Version = " + osVersion);
		bootSequence.add("Loading Kernal...");
		bootSequence.add("Loading Configuration...");
		
		int lastDelay = 0;
		for(int i = 0; i < bootSequence.size(); i++){
			final String s = bootSequence.get(i);
			
			int delay = Rand.range(50, 110);
			if(i == 2) delay = Rand.range(20, 30);
			
			Scheduler.delayedTask(() -> {
				write(s);
			}, lastDelay += delay);
		}
		
		Scheduler.delayedTask(() -> {
			runCommand("cls");
		}, lastDelay += Rand.range(50, 110));
		
		boolean hasStartupFile = CommandRun.canRun("startup.jas", this);
		
		List<String> bootSequence2 = new ArrayList<String>();
		bootSequence2.add("Starting " + Game.getName() + " version " + Game.getVersion() + " ...");
		bootSequence2.add("Loading commands...");
		bootSequence2.add("Indexing files...");
		bootSequence2.add((hasStartupFile ? "Running startup.jas ..." : "No startup.jas found.") + "\n");
		
		for(int i = 0; i < bootSequence2.size(); i++){
			final String s = bootSequence2.get(i);
			Scheduler.delayedTask(() -> {
				write(s);
			}, lastDelay += Rand.range(30, 70));
		}
		
		Scheduler.delayedTask(() -> {
			if(hasStartupFile) runCommand("run startup.jas -noDisp");
			
			while(runningCommand != null && runningCommand.running){}
			
			try {
				Thread.sleep(500);
			}catch (InterruptedException e) {}
			
			canInput = true;
		}, lastDelay += Rand.range(50, 110));
	}

	public void setRunning(Command cmd) {
		runningCommand = cmd;
	}
	
}
