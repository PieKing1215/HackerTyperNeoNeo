package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Rand;
import me.pieking.game.Utils;

public class FormattedString {

	static {
		letterGlitch = new boolean[20];
		letterGlitch2 = new boolean[20];
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
	
	public FormattedString(String baseString, boolean addBreaks){
		this.setRawString(baseString, addBreaks);
	}
	
	public void renderFormatted(Graphics2D g, int x, int y){
		renderFormatted(g, x, y, 1);
	}
	
	public static String addLinebreaks(String input, int maxLineLength) {
		return addLinebreaks(input, maxLineLength, true);
	}
	
	public static enum TextStyle{
		PLAIN, UNDERLINE, BOLD, ITALICS;
	}
	
	/**
	 * Splits text onto multiple lines, using the location of spaces.<br>
	 * Credit to <a href="https://stackoverflow.com/a/7528259">https://stackoverflow.com/a/7528259</a>.
	 */
	public static String addLinebreaks(String input, int maxLineLength, boolean removeSpecial) {
	    if(input.isEmpty()) return input;
		StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;

	    String[] spl = input.split(" (?!( |\n))"); // split at spaces before non-spaces (preserves multiple-space gaps)
	    
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
	    
	    if(output.toString().isEmpty()) return output.toString();
	    return output.toString().substring(0, output.toString().length()-1);
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
				}
				
    			for(int i = 0; i < letterGlitch2.length; i++){
    				letterGlitch2[i] = false;
    			}
			}
		}
	}
	
	public void addBreaks(){
		lastLineSize = Game.getFocusedArea().charPerLine;
		broken = addLinebreaks(getRawString(), Game.getFocusedArea().charPerLine, false);
		broken_f = addLinebreaks(getRawString(), Game.getFocusedArea().charPerLine, true);
	}
	
	public void renderFormatted(Graphics2D g, int x, int y, int padding){
		renderFormatted(g, x, y, padding, true);
	}
	
	public void renderFormatted(Graphics2D g, int x, int y, int padding, boolean doBreaks){

		if(alwaysRaw){
			renderRaw(g, x, y, padding, doBreaks);
			return;
		}
		
		List<TextStyle> styles = new ArrayList<TextStyle>();
		currColor = g.getColor();
		
		if(lastLineSize != Game.getFocusedArea().charPerLine && doBreaks) addBreaks();
		
//		String[] lines = getRawString().split("(?<=\\G.{54})"); //https://stackoverflow.com/a/3761521
		String[] lines = (doBreaks ? broken_f : baseString).split("\n");
		
		if(f != null) g.setFont(f);
		
		line: for(int l = 0; l < lines.length; l++){
		
			widthPrev = 0;
			
			String line = lines[l];
			int lineLength = line.length();
			
//			long total = 0;
			
			for(int i = 0; i < lineLength; i++){
//				long start = System.nanoTime();
				
				char text = line.charAt(i);
				String textS = text + "";
				char before = '\0';
				char twoBefore = '\0';
				
				if(i > 0) before = line.charAt(i-1);
				if(i > 1) twoBefore = line.charAt(i-2);
				
				boolean display = true;
				
				if((text == '\\' && before != '\\') || text == '¶'){
					display = false;
					if(i+1 < lineLength) {
						char next = line.charAt(i+1);
						
						if(next == 'b'){
							styles.add(TextStyle.BOLD);
						}else if(next == 'u'){
							styles.add(TextStyle.UNDERLINE);
						}else if(next == 'i'){
							styles.add(TextStyle.ITALICS);
						}else if(next == 'n'){
							styles.clear();
						}else if(next == '\\'){
							display = true;
						}else{
							currColor = Utils.parseColor(textS + line.charAt(i+1));
							g.setColor(currColor);
						}
					}
				}
				
				if((before == '\\' && twoBefore != '\\') || before == '¶' || before == '^' || text == '^') display = false;
				
//				if(before == '\\' && text == '\\') display = true;
				
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
    				
    				if(y + yOffset + yo > Game.getHeight()) return;
    				if(y + yOffset + yo < 0) continue line;
    				
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

    				int mod = 0;
    				if(styles.contains(TextStyle.ITALICS)) mod += Font.ITALIC;
    				g.setFont(g.getFont().deriveFont(mod));
    				
    				textS = new String(bytes);
    				g.drawString(textS, x + xOffset + xo, y + yOffset + yo);
					if(styles.contains(TextStyle.BOLD)) {
						g.drawString(textS, x + xOffset + xo, y + yOffset + yo-1);
						g.drawString(textS, x + xOffset + xo+1, y + yOffset + yo);
						g.drawString(textS, x + xOffset + xo+1, y + yOffset + yo-1);
					}
					if(styles.contains(TextStyle.UNDERLINE)){
						g.drawLine(x + xOffset + xo + g.getFontMetrics().stringWidth(textS), y + yOffset + yo + 2, x + xOffset + xo, y + yOffset + yo + 2);
					}
				}
				
			}
			
//			System.out.println(total/lineLength);
		}
	}
	
	public void renderRaw(Graphics g, int x, int y){
		renderRaw(g, x, y, 1);
	}
	
	public void renderRaw(Graphics g, int x, int y, int padding){
		renderRaw(g, x, y, padding, true);
	}
	
	public void renderRaw(Graphics g, int x, int y, int padding, boolean doBreaks){

		if(lastLineSize != Game.getFocusedArea().charPerLine) addBreaks();
//		String[] lines = getRawString().split("(?<=\\G.{54})"); // https://stackoverflow.com/a/3761521
		String[] lines = (doBreaks ? broken : baseString).split("\n");
		
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
		setRawString(baseString, true);
	}
	
	public void setRawString(String baseString, boolean addBreaks) {
		this.baseString = baseString;
		if(!this.baseString.isEmpty() && addBreaks) addBreaks();
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

	public FormattedString[] split(String string) {
		return split(string, true);
	}
	
	public FormattedString[] split(String string, boolean addBreaks) {
		String[] spl = baseString.split("\n", -1);
		FormattedString[] ret = new FormattedString[spl.length];
		for(int i = 0; i < spl.length; i++){
			ret[i] = new FormattedString(spl[i], addBreaks);
		}
		return ret;
	}

	public void append(String str) {
		baseString += str;
	}
	
}
