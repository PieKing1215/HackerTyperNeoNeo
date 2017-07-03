package me.pieking.game.console;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.FormattedString;

public class TextEditor extends TextArea {

	File file;
	
	boolean justSaved = false;
	
	String msg;
	int msgTimer = -1;
	
	String lastSaved = "";
	boolean inSaveDialogue = false;
	boolean inExitDialogue = false;
	
	public TextEditor() {
		autoWrapLines = false;
	}
	
	public void setFile(String str){
		File newDir = new File(Game.getFileDir(), str);
		newDir.mkdir();
		if(!newDir.isDirectory() && newDir.exists()){
			file = newDir;
			cursorIndex = 0;
			try {
				String raw = Utils.readFile(file);
				try{
					typing = raw.substring(0, raw.length()-1);
				}catch(Exception e){
					typing = "";
				}
//				System.out.println("|" + typing + "|");
			}catch (IOException e) {
				e.printStackTrace();
				showMessage("Could not load file: " + e.getMessage());
				typing = "";
			}
			lastSaved = typing;
			formatTimer = 2;
		}
	}
	
	Pattern p1 = Pattern.compile("(\\\\{2,})");
	Pattern p2 = Pattern.compile("(?<!\\\\)(\\\\)(?!\\\\)");
	Pattern p3 = Pattern.compile("(?<=\\s)(print|input|run|int|rnd|remove|sleep)(?=\\s)");
	Pattern p4 = Pattern.compile("(?<=\\s)(goto|sub|if|then|return)(?=\\s)");
	Pattern p5 = Pattern.compile("^( +?(?<!\\\\)\\w+:)", Pattern.MULTILINE);
	Pattern p6 = Pattern.compile("((?<!\\\\)[+\\-\\*/<>=#()])");
	Pattern p12 = Pattern.compile("((?<!\\\\)[\\[\\]{}])");
	Pattern p13 = Pattern.compile("(?<!\\\\)\\[(\\?)\\]");
	Pattern p7 = Pattern.compile("((?<=[0-9\\s\\[\\{,])[0-9])");
	
	Pattern p8 = Pattern.compile("(\".*?)(?<!\\\\)(\\\\(?!\\\\).)(.*?\")");
	Pattern p9 = Pattern.compile("((?<=[\\s+=])\".*?\")");
	Pattern p10 = Pattern.compile("('.*?)(\\\\.)");
	Pattern p11 = Pattern.compile("((?<=\\s)'.+)");
	
	String lastRaw = "";
	String lastDisplay = "";
	
	boolean formatting = true;
	
	int formatTimer = -1;
	
	@Override
	public void tick() {
		
		text.setRawString(typing);
		
		boolean nowSave = Game.keyHandler().isPressed(KeyEvent.VK_CONTROL) && Game.keyHandler().isPressed(KeyEvent.VK_S);
		
		if(nowSave && !justSaved){
			try{
				save();
			}catch(Exception e){
				e.printStackTrace();
				showMessage("Could not save file: " + e.getMessage());
			}
		}
		
		justSaved = nowSave;
		
		if(msgTimer > -1) msgTimer--;
		
		if(formatTimer > -1) formatTimer--;
		
		if(formatting && file.getName().endsWith(".jas") && formatTimer == 0){
    		new Thread(() -> {
    			try{
    				
    				String raw = super.getDisplayText();
    				
					raw = p1.matcher(raw).replaceAll("$1\\\\");
					raw = p2.matcher(raw).replaceAll("\\\\\\\\");
					String tRaw = raw;
    				
    				String base = tRaw;
        			long rStart = System.currentTimeMillis();
        			
//        			base = p1.matcher(base).replaceAll("$1\\\\");
//        			base = p2.matcher(base).replaceAll("\\\\\\\\");
        			
        			base = p3.matcher(base).replaceAll("\\\\L$1\\\\W");
        			base = p4.matcher(base).replaceAll("\\\\l$1\\\\W");
        			
        			base = p5.matcher(base).replaceAll("\\\\P$1\\\\W");
        			base = p6.matcher(base).replaceAll("\\\\R$1\\\\W");
        			base = p13.matcher(base).replaceAll("[\\\\G$1\\\\W]");
        			base = p7.matcher(base).replaceAll("\\\\p$1\\\\W");
        			base = p12.matcher(base).replaceAll("\\\\Y$1\\\\W");
            		
        			Matcher m = p10.matcher(base);
        			long start = System.currentTimeMillis();
            		while(m.find() && System.currentTimeMillis() - start < 1000){
            			base = m.replaceAll("$1");
            			m.reset(base);
            		}
            		
            		base = p11.matcher(base).replaceAll("\\\\6$1\\\\W");
        			
            		start = System.currentTimeMillis();
            		m = p8.matcher(base);
            		while(m.find() && System.currentTimeMillis() - start < 1000){
            			base = m.replaceAll("$1$3");
            			m.reset(base);
            		}
            		
            		base = base.replace("+\\W\"", "+\"");
            		base = base.replace("=\\W\"", "=\"");
            		base = p9.matcher(base).replaceAll("\\\\O$1\\\\W");
            		
            		//System.out.println(System.currentTimeMillis()-rStart);
            		
            		lastDisplay = base;
        		}catch (Exception e1){
        			e1.printStackTrace();
        		}
    		}).start();
		}
		
		super.tick();
	}
	
	@Override
	public void type(KeyEvent e) {
		if(!inSaveDialogue && !inExitDialogue){ 
			super.type(e);
			
//			System.out.println(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL) + " " + e.getKeyChar()+"");
			
			if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL) && (int)e.getKeyChar() == 6 /* CTRL + f */){
				formatting = !formatting;
				lastRaw += " ";
				showMessage("Formatting " + (formatting ? "enabled" : "disabled") + ".");
			}
			
		}else{
			if((int)e.getKeyChar() == KeyEvent.VK_ESCAPE) {
				escape();
			}else if((e.getKeyChar()+"").equalsIgnoreCase("Y")){
				if(inSaveDialogue){
					inExitDialogue = true;
					inSaveDialogue = false;
					try {
						save();
					}catch (IOException e1) {
						e1.printStackTrace();
						showMessage("Could not save file: " + e1.getMessage());
					}
				}else if(inExitDialogue){
					inExitDialogue = false;
					Game.setFocusedArea(Game.getMainConsole());
				}
			}else if((e.getKeyChar()+"").equalsIgnoreCase("N")){
				if(inSaveDialogue){
					inExitDialogue = true;
					inSaveDialogue = false;
				}else if(inExitDialogue){
					inExitDialogue = false;
				}
			}
		}
		
		if(formatting) formatTimer = 10;
		
		String raw = super.getDisplayText();
		
		if(!lastRaw.equals(raw)){
			lastRaw = raw;
			
			raw = p1.matcher(raw).replaceAll("$1\\\\");
			raw = p2.matcher(raw).replaceAll("\\\\\\\\");
			
			String tRaw = raw;
			
			lastDisplay = tRaw;
		}
		
	}
	
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		
		if(msgTimer > -1){
			float thru = 1f;
			
			if(msgTimer < 30) thru = msgTimer / 30f;
			
			if(thru < 0) thru = 0;
			if(thru > 1) thru = 1;
			
			g.setFont(Fonts.anonymous.deriveFont(24f));
			g.setColor(new Color(0.5f, 0.5f, 0.5f, thru/2f));
			g.fillRect(0, Game.getHeight() - 40, g.getFontMetrics().stringWidth(msg) + 20, 40);
			g.setColor(new Color(0f, 0f, 0f, thru/2f));
			g.drawString(msg, 12, Game.getHeight() - 14);
			g.drawString(msg, 12, Game.getHeight() - 10);
			g.drawString(msg, 10, Game.getHeight() - 12);
			g.drawString(msg, 14, Game.getHeight() - 12);
			g.setColor(new Color(1f, 1f, 1f, thru));
			g.drawString(msg, 12, Game.getHeight() - 12);
		}
		
		if(inSaveDialogue){
			g.setColor(new Color(0f, 0f, 0f, 0.5f));
			g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
			g.setColor(Color.WHITE);
    		g.setFont(Fonts.anonymous.deriveFont(24f));
    		String msg = "Save? (Y/N)";
    		g.drawString(msg, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg)/2, 200);
		}else if(inExitDialogue){
			g.setColor(new Color(0f, 0f, 0f, 0.5f));
			g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
			g.setColor(Color.WHITE);
    		g.setFont(Fonts.anonymous.deriveFont(24f));
    		String msg = "Exit? (Y/N)";
    		g.drawString(msg, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg)/2, 200);
		}
		
	}
	
	public void save() throws IOException{
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(typing);
		bw.close();
		fw.close();
		
		lastSaved = typing;
		
		showMessage("Saved " + file.getName());
	}
	
	public void showMessage(String msg){
		this.msg = msg;
		this.msgTimer = 120;
	}
	
	@Override
	public void escape() {
		if(!inSaveDialogue && !inExitDialogue){
    		if(lastSaved.equals(typing)){
    			inExitDialogue = true;
    		}else{
    			inSaveDialogue = true;
    		}
		}else{
			if(inSaveDialogue){
				inExitDialogue = false;
				inSaveDialogue = false;
			}else if(inExitDialogue){
				inExitDialogue = false;
			}
		}
	}
	
	@Override
	public String getDisplayText() {
		return lastDisplay;
	}
	
}
