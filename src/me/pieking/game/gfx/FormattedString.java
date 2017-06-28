package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import me.pieking.game.Game;
import me.pieking.game.Rand;
import me.pieking.game.Utils;

public class FormattedString {

	static {
		letterGlitch = new boolean[20];
		updateRand();
	}
	
	private String baseString;

	private TextEffect effect = TextEffect.NONE;
	
	private int widthPrev = 0;
	
	private Color currColor = Color.WHITE;
	
	private Font f;
	
	public int lastLineSize = 0;
	public String broken_f;
	public String broken;
	
	/**
	 * Key:<br>
	 * <b>&</b> = Next Line<br>
	 * <b>�(hex code)</b> = Color with value 0-f (See Vars.Color enum)<br>
	 */
	public FormattedString(String baseString){
		this.setRawString(baseString);
	}
	
	public void renderFormatted(Graphics2D g, int x, int y){
		renderFormatted(g, x, y, 1);
	}
	
	public static String addLinebreaks(String input, int maxLineLength) {
		return addLinebreaks(input, maxLineLength, true);
	}
	
	/**
	 * Splits text onto multiple lines, using the location of spaces.<br>
	 * Credit to <a href="https://stackoverflow.com/a/7528259">https://stackoverflow.com/a/7528259</a>.
	 */
	public static String addLinebreaks(String input, int maxLineLength, boolean removeSpecial) {
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;

	    String[] spl = input.split(" (?! )"); // split at spaces before non-spaces (preserves multiple-space gaps)
	    
	    for (String s : spl) {
	        String word = s + " ";

	        int length = (removeSpecial ? strip(word) : word).length();
	        
	        if (lineLen + length > maxLineLength) {
	            output.append("\n");
	            lineLen = 0;
	        }
	        
	        output.append(word);
	        lineLen += length;
	    }
	    
	    return output.toString();
	}
	
	public static boolean alwaysRaw = false;
	public static boolean[] letterGlitch = new boolean[20];
	public static boolean[] letterGlitch2 = new boolean[20];
	
	public static void updateRand(){
		int glitchLevels = Game.glitchLevels;
		if(Game.getTime() % 10 == 0){
			if(glitchLevels > 0){
    			alwaysRaw = Rand.getRand().nextBoolean() && Rand.oneIn((100 - glitchLevels)/2);
    			
    			for(int i = 0; i < letterGlitch.length; i++){
    				letterGlitch[i] = Rand.getRand().nextBoolean() && Rand.oneIn((100 - glitchLevels));
    				if(glitchLevels < 20) letterGlitch[i] = letterGlitch[i] && Rand.oneIn(4);
    				letterGlitch2[i] = Rand.getRand().nextBoolean() && Rand.oneIn((100 - glitchLevels));
    				if(glitchLevels < 20) letterGlitch2[i] = letterGlitch2[i] && Rand.oneIn(4);
    			}
			}else{
				alwaysRaw = false;
    			
    			for(int i = 0; i < letterGlitch.length; i++){
    				letterGlitch[i] = false;
    				letterGlitch2[i] = false;
    			}
			}
		}
	}
	
	public void addBreaks(){
		lastLineSize = Game.focusedConsole().charPerLine;
		broken = addLinebreaks(getRawString(), Game.focusedConsole().charPerLine, false);
		broken_f = addLinebreaks(getRawString(), Game.focusedConsole().charPerLine, true);
	}
	
	public void renderFormatted(Graphics2D g, int x, int y, int padding){

		if(alwaysRaw){
			renderRaw(g, x, y, padding);
			return;
		}
		
		currColor = g.getColor();
		
		if(lastLineSize != Game.focusedConsole().charPerLine) addBreaks();
		
//		String[] lines = getRawString().split("(?<=\\G.{54})"); //https://stackoverflow.com/a/3761521
		String[] lines = broken_f.split("\n");
		
		if(f != null) g.setFont(f);
		
		for(int l = 0; l < lines.length; l++){
		
			widthPrev = 0;
			
			String line = lines[l];
			int lineLength = line.length();
			
//			long total = 0;
			
			for(int i = 0; i < lineLength; i++){
//				long start = System.nanoTime();
				
				char text = line.charAt(i);
				String textS = text + "";
				char before = '\0';
				
				if(i > 0) before = line.charAt(i-1);
				
				boolean display = true;
				
				if(text == '\\' || text == '¶'){
					if(i+1 < lineLength) {
						currColor = Utils.parseColor(textS + line.charAt(i+1));
						g.setColor(currColor);
					}
					display = false;
				}
				
				if(before == '\\' || before == '¶' || before == '^' || text == '^') display = false;
				
				int xOffset = widthPrev;
				
//				total += System.nanoTime() - start;
				
				if(display){
					widthPrev += g.getFontMetrics().stringWidth(textS) + padding;
				
    				int yOffset = Math.round((float) ((g.getFont().getSize() * 1.15) * l));
    				
    				int xo = 0;
    				int yo = 0;
    				
    				switch(getEffect()){
    				case HEAVY_WAVE:
    					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
    					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
    					break;
    				case WAVE:
    					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*2);
    					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*2);
    					break;
    				case SMALL_CIRCLE:
    					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f));
    					yo = Math.round((float) Math.cos((Game.getTime() + 3*i) / 10f));
    					break;
    				case LARGE_CIRCLE:
    					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*3);
    					yo = Math.round((float) Math.cos((Game.getTime() + 3*i) / 10f)*3);
    					break;
    				case NONE:
    				default:
    					break;
    				}
    				
    				byte[] bytes = textS.getBytes();
    				
    				try{
        				int randIndex = (i + (y + yOffset + x + xOffset + xo)) % letterGlitch.length;
        				boolean glitch = letterGlitch[randIndex];
        				if(glitch && !textS.equals(" ")){
        					bytes[0]++;
        				}
        				
        				if(Game.glitchLevels > 75){
            				int exclIndex = (i + (y + yOffset*22 + x*((Game.getTime()/10)%60) + xOffset + xo)) % letterGlitch.length;
            				boolean excl = letterGlitch2[exclIndex];
            				if(excl) bytes[0] = '!';
        				}
        				
    				}catch(Exception e){}
    				
    				textS = new String(bytes);
    				
					g.drawString(textS, x + xOffset + xo, y + yOffset + yo);
				}
				
			}
			
//			System.out.println(total/lineLength);
		}
	}
	
	public void renderRaw(Graphics g, int x, int y){
		renderRaw(g, x, y, 1);
	}
	
	public void renderRaw(Graphics g, int x, int y, int padding){

		if(lastLineSize != Game.focusedConsole().charPerLine) addBreaks();
//		String[] lines = getRawString().split("(?<=\\G.{54})"); // https://stackoverflow.com/a/3761521
		String[] lines = broken.split("\n");
		
		if(f != null) g.setFont(f);
		
		for(int l = 0; l < lines.length; l++){
		
			widthPrev = 0;
			
			String line = lines[l];
			int lineLength = line.length();
			
			for(int i = 0; i < lineLength; i++){
				String textS = line.charAt(i) + "";
				
				int xOffset = widthPrev;
				
				widthPrev += g.getFontMetrics().stringWidth(textS) + padding;
			
				int yOffset = Math.round((float) ((g.getFont().getSize() * 1.15) * l));
				
				int xo = 0;
				int yo = 0;
				
				switch(getEffect()){
				case HEAVY_WAVE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					break;
				case WAVE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*2);
					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*2);
					break;
				case SMALL_CIRCLE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f));
					yo = Math.round((float) Math.cos((Game.getTime() + 3*i) / 10f));
					break;
				case LARGE_CIRCLE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*3);
					yo = Math.round((float) Math.cos((Game.getTime() + 3*i) / 10f)*3);
					break;
				case NONE:
				default:
					break;
				}
				
				try{
    				byte[] bytes = textS.getBytes();
    				int randIndex = (i + (y + yOffset + x + xOffset + xo)) % letterGlitch.length;
    				boolean glitch = letterGlitch[randIndex];
    				if(glitch && !textS.equals(" ")){
    					bytes[0]++;
    				}
    				textS = new String(bytes);
				}catch(Exception e){}
				
				g.drawString(textS, x + xOffset + xo, y + yOffset + yo);
				
			}
			
//			System.out.println(total/lineLength);
		}
	}
	
	public void tick(){
		
	}

	public TextEffect getEffect() {
		return effect;
	}

	public void setEffect(TextEffect effect) {
		this.effect = effect;
	}

	public Font getFont() {
		return f;
	}

	public void setFont(Font f) {
		this.f = f;
	}

	public String getRawString() {
		return baseString;
	}

	public void setRawString(String baseString) {
		this.baseString = baseString;
		addBreaks();
	}
	
	public static enum TextEffect {
        NONE, SMALL_CIRCLE, LARGE_CIRCLE, WAVE, HEAVY_WAVE; 
    }

	public String getStripped() {
		return strip(baseString);
	}
	
	public static String strip(String s){
		String ret = s;
		
		ret = ret.replaceAll("\\\\.", "");
		ret = ret.replaceAll("\\¶.", "");
		ret = ret.replaceAll("^", "");
		
		return ret;
	}
	
}
