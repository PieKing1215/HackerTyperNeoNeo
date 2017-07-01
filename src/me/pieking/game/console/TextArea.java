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
import java.io.IOException;

import me.pieking.game.Game;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.FormattedString;

public class TextArea {

	public String typing = "";
	public int cursorIndex = typing.length();
	
	public int blinkTimer = 0;
	public int fontSize = 20;
	public int maxScroll;
	
	public int scrollOfs = 0;
	
	public boolean canInput = false;
	
	public FormattedString text = new FormattedString("");
	public int maxLines = 5;
	public boolean deleteLines = false;
	
	public int charPerLine = 78;
	public boolean autoWrapLines = true;
	
	public void tick(){
		if(scrollOfs < 0 && text.getRawString().isEmpty()) scrollOfs = 0;
		blinkTimer++;
		fontSize = 14;
		maxLines = 128;
		charPerLine = 78;
	}
	
	public void render(Graphics2D g){
		
//		FormattedString[] str = getLines();
		int lastOfs = 0;
//		for(int i = 0; i < str.length; i++){
//			FormattedString line = str[i];
//			
//			g.setFont(Fonts.anonymous.deriveFont((float)fontSize));
//			
//			int h = height(line, g.getFont());
//			
//			int y = fontSize*2 + lastOfs + scrollOfs;
//			
//			try{
//				if(y > 0 && y - fontSize < Game.getHeight()) {
//					line.renderFormatted(g, fontSize, y);
//				}
//			}catch(Exception e){}
//			
//			lastOfs += h;
//		}
		
		FormattedString line = new FormattedString(getDisplayText());
		
		g.setFont(Fonts.anonymous.deriveFont((float)fontSize));
		
		line.renderFormatted(g, fontSize, fontSize*2 + lastOfs + scrollOfs, 1, autoWrapLines);
		
		int fontW = g.getFontMetrics().stringWidth("w")+1;
		
		String broken = "";
		
		if(!autoWrapLines) {
			try{
				broken = typing.substring(0, cursorIndex);
			}catch(StringIndexOutOfBoundsException e){
				broken = typing;
			}
		}else{
			try{
				broken = FormattedString.addLinebreaks(typing, charPerLine).replace(" \n", "\n").substring(0, cursorIndex);
			}catch(StringIndexOutOfBoundsException e){
				broken = FormattedString.addLinebreaks(typing, charPerLine).replace(" \n", "\n");
			}
		}
		
		int totalBreaks = broken.length() - broken.replace("\n", "").length();
		int lastBreak = broken.lastIndexOf("\n");
		int yOfs = lastOfs + (int) (totalBreaks * (g.getFont().getSize() * 1.15));
	
		if(blinkTimer % 60 < 30){
			int ofs = 0-1;
			if(totalBreaks > 0) ofs = -1;
			if(cursorIndex == getDisplayText().length()){
				g.fillRect(fontSize + (cursorIndex + ofs - lastBreak)*fontW, fontSize*2 + scrollOfs + yOfs, fontW, 2);
			}else{
				g.fillRect(fontSize + (cursorIndex + ofs - lastBreak)*fontW - 2, 2 + scrollOfs + yOfs + fontSize, 2, fontSize);
			}
		}
		
		int minScroll = -(maxScroll-Game.getHeight() + fontSize*4);
		int maxScroll = 0;

		int dif = maxScroll - minScroll;
		float thru = -scrollOfs/(float)dif;
		
		if(this.maxScroll >= Game.getHeight()-fontSize*3){
    		int size = Math.max(minScroll + 600, 48);
    		
    		g.setColor(Color.DARK_GRAY);
    		g.fillRect(Game.getWidth()-8, 0, 8, Game.getHeight());
    		
    		int ofs = (int) (thru * (Game.getHeight()-size));
    		g.setColor(Color.GRAY);
    		g.fill3DRect(Game.getWidth()-8, ofs, 8, size, true);
		}
		//System.out.println(scrollOfs);
		
	}
	
	public String getDisplayText() {
		return typing;
	}
	
	public FormattedString[] getLines(){
		return text.split("\n", autoWrapLines);
	}
	
	public void scroll(MouseWheelEvent e) {
		scroll(e.getWheelRotation() * 3);
	}
	
	public void scroll(int scrollAmount) {
		int newScroll = (int) (scrollOfs - scrollAmount * (fontSize * 1.15f));
	
		if(scrollAmount < 0) newScroll--;
		
//		System.out.println(newScroll + " " + maxScroll);

		int newMaxScroll = 0;
		
		FormattedString[] str = getLines();
		for(int i = 0; i < str.length; i++){
			FormattedString line = str[i];
			if(line.getRawString().isEmpty()) line.setRawString(" ", autoWrapLines);
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize  + newMaxScroll + newScroll;
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight();
				//newScroll -= diff;
				y -= diff;
			}
			newMaxScroll += h;
		}
		
		if(scrollAmount > 0 && newScroll < -(newMaxScroll-Game.getHeight() + fontSize*4)) newScroll = -(newMaxScroll-Game.getHeight() + fontSize*4); //TODO: figure out how to calculate this magic number 
		
		if(newScroll > 0) newScroll = 0;
		
		if(newMaxScroll < Game.getHeight()-fontSize*3) newScroll = 0;
		
		maxScroll = newMaxScroll;
		
		
		scrollOfs = newScroll;
	}
	
	public void clear() {
		text.setRawString("", autoWrapLines);
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
	
	public void left() {
		blinkTimer = 0;
		
		if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
			if(cursorIndex > 0){
    			String sub = typing.substring(0, cursorIndex-1);
    			int space = sub.lastIndexOf(" ");
    			int newLine = sub.lastIndexOf("\n");
    			
    			cursorIndex = Math.max(Math.max(newLine+1, space+1), 0);
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
    			int newLine = sub.indexOf("\n");
    			
//    			System.out.println(space);
    			
    			if(newLine != -1 && space != -1){
    				cursorIndex += Math.min(space, newLine)+1;
    			}else if(space != -1){
    				cursorIndex += space+1;
    			}else if(newLine != -1){
    				cursorIndex += newLine+1;
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
	
	public void type(KeyEvent e){
		
		StringBuilder sb = new StringBuilder(typing);
		
		if((int)e.getKeyChar() == KeyEvent.VK_ESCAPE /* http://stackoverflow.com/a/15693905 */){
			escape();
			return;
		}
		
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
		}else if((int)e.getKeyChar() == KeyEvent.VK_ENTER){
			enter(sb);
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
//		scroll(maxLines);
//		System.out.println(typing);
	}
	
	public void escape() {
		typing = "";
		cursorIndex = 0;
	}

	public void write(String s){
		text.append(s + "\\W\\n" + "\n");
//		text.setRawString(text.getRawString().substring(0, text.getRawString().length()-1));
		
		while(getLines().length > maxLines && deleteLines ){
			String[] spl = text.getRawString().split("\n");
			String newText = "";
			for(int i = 1; i < spl.length; i++){
				newText += spl[i] + "\n";
			}
			//newText = newText.substring(0, newText.length()-1);
			text.setRawString(newText, autoWrapLines);
		}
		
		//System.out.println(fontSize + (fontSize * maxLines) + scrollOfs);
		
//		if(fontSize + (fontSize * lines.size()) + scrollOfs > Game.getHeight()-fontSize){
//			scrollOfs -= fontSize;
//		}
		
		FormattedString[] str = getLines();
		int lastOfs = 0;
		
		for(int i = 0; i < str.length; i++){
			FormattedString line = str[i];
			int h = height(line, Fonts.anonymous.deriveFont((float)fontSize));
			int y = fontSize*2 + lastOfs + scrollOfs;
			if(y+h > Game.getHeight()){
				int diff = (y+h) - Game.getHeight()+fontSize;
				scrollOfs -= diff;
				y -= diff;
			}
			lastOfs += h;
		}
	}
	
	public void setMaxLines(int lines){
		this.maxLines = lines;
	}
	
	public int getMaxLines(){
		return this.maxLines;
	}
	
	public void enter(StringBuilder sb) {
		sb.insert(cursorIndex, "\n");
		cursorIndex++;
	}
	
}
