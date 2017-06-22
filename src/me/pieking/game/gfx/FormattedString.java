package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.StringTokenizer;

import me.pieking.game.Game;
import me.pieking.game.Rand;
import me.pieking.game.Utils;

public class FormattedString {

	private static Sprite inf = Images.getSprite("source/sprites/spr_infinitysign_0.png");
	
	private String baseString;

	private int[] shakeMovementsX, shakeMovementsY;

	private TextEffect effect = TextEffect.NONE;
	
	private int widthPrev = 0;
	
	private Color currColor = Color.WHITE;
	
	private Font f;
	
	/**
	 * Key:<br>
	 * <b>&</b> = Next Line<br>
	 * <b>�(hex code)</b> = Color with value 0-f (See Vars.Color enum)<br>
	 */
	public FormattedString(String baseString){
		this.setRawString(baseString);
		//System.out.println(this.baseString);
		
		shakeMovementsX = new int[baseString.length()];
		shakeMovementsY = new int[baseString.length()];
	}
	
	public void renderFormatted(Graphics g, int x, int y){
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
	    StringTokenizer tok = new StringTokenizer(input, " ");
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;
	    while (tok.hasMoreTokens()) {
	        String word = tok.nextToken() + " ";

	        int length = word.length();
	        
	        if(removeSpecial) {
	        	length = word.replaceAll("\\\\.", "").length();
	        }
	        
	        if (lineLen + length > maxLineLength) {
	            output.append("\n");
	            lineLen = 0;
	        }
	        output.append(word);
	        lineLen += length;
	    }
	    return output.toString();
	}
	
	public void renderFormatted(Graphics g, int x, int y, int padding){

		currColor = g.getColor();
		
//		String[] lines = getRawString().split("(?<=\\G.{54})"); //https://stackoverflow.com/a/3761521
		String[] lines = addLinebreaks(getRawString(), Game.focusedConsole().charPerLine).split("\n"); //https://stackoverflow.com/a/3761521
		
		for(int l = 0; l < lines.length; l++){
		
			widthPrev = 0;
			
			String line = lines[l];
			
			for(int i = 0; i < line.length(); i++){
				
				int globalIndex = i;
				if(l > 0) globalIndex += lines[l-1].length();
				
				String text = line.charAt(i) + "";
				Color col = null;
				
				String charBefore = "";
				
				if(i > 0) charBefore = line.charAt(i-1) + "";
				
				boolean display = true;
				
				if(text.equals("\\") || text.equals("�")){
					if(i+1 < line.length()){
						String code = line.charAt(i+1) + "";
						col = Utils.parseColor(text + code);
					}
					
					display = false;
				}
				
				if(charBefore.equals("\\") || charBefore.equals("�") || text.equals("^") || charBefore.equals("^")) display = false;
				
				int xOffset = widthPrev;
				
				if(display){
					if(getFont() != null){
						//System.out.println("f");
						if(text.equals("\u221E")){
							widthPrev += (inf.getWidth() * 2) + 2 + padding;
						}else{
							widthPrev += g.getFontMetrics(getFont()).stringWidth(text) + padding;
						}
					}else{
						if(text.equals("\u221E")){
							widthPrev += (inf.getWidth() * 2) + 2 + padding;
						}else{
							widthPrev += g.getFontMetrics().stringWidth(text) + padding;
						}
					}
				}
				
				if(getEffect() == TextEffect.TWITCH){
					if(Game.getTime() % 4 == 0){
						for(int so = 0; so < getRawString().length(); so++){
							shakeMovementsX[so] = Rand.range(-1, 1);
							shakeMovementsY[so] = Rand.range(-1, 1);
						}
					}
				}else if(getEffect() == TextEffect.SHAKE){
					if(Game.getTime() % 60 == 0){
						for(int so = 0; so < getRawString().length(); so++){
							if(Rand.oneIn(1000)) shakeMovementsX[so] = Rand.range(-2, 2);
							if(Rand.oneIn(1000)) shakeMovementsY[so] = Rand.range(-2, 2);
						}
					}
				}
				
				int yOffset = Math.round((float) ((g.getFont().getSize() * 1.15) * l));
				
				int xo = 0;
				int yo = 0;
				
				switch(getEffect()){
				case SHAKE:
					xo = shakeMovementsX[globalIndex];
					yo = shakeMovementsY[globalIndex];
					break;
				case HEAVY_WAVE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					break;
				case TWITCH:
					xo = shakeMovementsX[globalIndex] * 2;
					yo = shakeMovementsY[globalIndex] * 2;
					break;
				case NONE:
					
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
				case ASRIEL_DREEMURR:
					if(getFont() != null) g.setFont(getFont());
					
					for(int i2 = 0; i2 < 5; i2++){
						g.setColor(new Color(1f, 1f, 1f, (1f - (i2 / 5f)) * 0.5f));
						xo = Rand.range(-10, 10);
						yo = Rand.range(-10, 10);
						
						if(display) g.drawString(text, x + xOffset + xo, y + yOffset + yo);
					}
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					
					currColor = Color.WHITE;
					break;
				case GASTER:
					if(getFont() != null) g.setFont(getFont());
					
					g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 127));
					String text2 = text;
					if(Rand.range(0, 0) == 0){
//						g.setFont(Fonts.wingDings);
//						text2 = Fonts.execFormatValue(text2, Fonts.wingDings);
					}else{
						if(getFont() != null) g.setFont(getFont());
					}
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					
					if(display) g.drawString(text2, x + xOffset + xo, y + yOffset + yo);
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					break;
				default:
					break;
				}
				
				if(col != null) currColor = col;
				
				if(display){
					g.setColor(currColor);
					Font f = getFont();
					if(f != null) g.setFont(f);
					
					if(getEffect() == TextEffect.GASTER){
						if(Rand.range(0, 2) == 0){
//							g.setFont(Fonts.wingDings);
//							text = Fonts.execFormatValue(text, Fonts.wingDings);
						}
					}
					
					if(text.equals("\u221E")){
						g.drawImage(inf.getImage(), x + xOffset + xo, y + yOffset + yo - (inf.getHeight()*2), inf.getWidth() * 2, inf.getHeight() * 2, null);
					}else{
						g.drawString(text, x + xOffset + xo, y + yOffset + yo);
					}
				}
			}
		}
	}
	
	public void renderRaw(Graphics g, int x, int y){
		renderRaw(g, x, y, 1);
	}
	
	public void renderRaw(Graphics g, int x, int y, int padding){

//		String[] lines = getRawString().split("(?<=\\G.{54})"); // https://stackoverflow.com/a/3761521
		String[] lines = addLinebreaks(getRawString(), Game.focusedConsole().charPerLine, false).split("\n");
		
		for(int l = 0; l < lines.length; l++){
		
			widthPrev = 0;
			
			for(int i = 0; i < lines[l].length(); i++){
				
				int globalIndex = i;
				try{
					globalIndex += lines[l-1].length();
				}catch(ArrayIndexOutOfBoundsException e){}
				
				String text = lines[l].charAt(i) + "";
				Color col = null;
				
				boolean display = true;
				
				int xOffset = widthPrev;
				
				if(display){
					if(getFont() != null){
						//System.out.println("f");
						if(text.equals("\u221E")){
							widthPrev += (inf.getWidth() * 2) + 2 + padding;
						}else{
							widthPrev += g.getFontMetrics(getFont()).stringWidth(text) + padding;
						}
					}else{
						if(text.equals("\u221E")){
							widthPrev += (inf.getWidth() * 2) + 2 + padding;
						}else{
							widthPrev += g.getFontMetrics().stringWidth(text) + padding;
						}
					}
				}
				
				if(getEffect() == TextEffect.TWITCH){
					if(Game.getTime() % 4 == 0){
						for(int so = 0; so < getRawString().length(); so++){
							shakeMovementsX[so] = Rand.range(-1, 1);
							shakeMovementsY[so] = Rand.range(-1, 1);
						}
					}
				}else if(getEffect() == TextEffect.SHAKE){
					if(Game.getTime() % 60 == 0){
						for(int so = 0; so < getRawString().length(); so++){
							if(Rand.oneIn(1000)) shakeMovementsX[so] = Rand.range(-2, 2);
							if(Rand.oneIn(1000)) shakeMovementsY[so] = Rand.range(-2, 2);
						}
					}
				}
				
				int yOffset = Math.round((float) ((g.getFont().getSize() * 1.15) * l));
				
				int xo = 0;
				int yo = 0;
				
				switch(getEffect()){
				case SHAKE:
					xo = shakeMovementsX[globalIndex];
					yo = shakeMovementsY[globalIndex];
					break;
				case HEAVY_WAVE:
					xo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					yo = Math.round((float) Math.sin((Game.getTime() + 3*i) / 10f)*5);
					break;
				case TWITCH:
					xo = shakeMovementsX[globalIndex] * 2;
					yo = shakeMovementsY[globalIndex] * 2;
					break;
				case NONE:
					
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
				case ASRIEL_DREEMURR:
					if(getFont() != null) g.setFont(getFont());
					
					for(int i2 = 0; i2 < 5; i2++){
						g.setColor(new Color(1f, 1f, 1f, (1f - (i2 / 5f)) * 0.5f));
						xo = Rand.range(-10, 10);
						yo = Rand.range(-10, 10);
						
						if(display) g.drawString(text, x + xOffset + xo, y + yOffset + yo);
					}
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					
					currColor = Color.WHITE;
					break;
				case GASTER:
					if(getFont() != null) g.setFont(getFont());
					
					g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 127));
					String text2 = text;
					if(Rand.range(0, 0) == 0){
//						g.setFont(Fonts.wingDings);
//						text2 = Fonts.execFormatValue(text2, Fonts.wingDings);
					}else{
						if(getFont() != null) g.setFont(getFont());
					}
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					
					if(display) g.drawString(text2, x + xOffset + xo, y + yOffset + yo);
					
					xo = Rand.range(-1, 2);
					yo = Rand.range(-1, 2);
					break;
				default:
					break;
				}
				
				if(col != null){
					currColor = col;
				}
				
				if(display){
					g.setColor(currColor);
					if(getFont() != null) g.setFont(getFont());
					
					if(getEffect() == TextEffect.GASTER){
						if(Rand.range(0, 2) == 0){
//							g.setFont(Fonts.wingDings);
//							text = Fonts.execFormatValue(text, Fonts.wingDings);
						}else{
							if(getFont() != null) g.setFont(getFont());
						}
					}
					
					if(text.equals("\u221E")){
						g.drawImage(inf.getImage(), x + xOffset + xo, y + yOffset + yo - (inf.getHeight()*2), inf.getWidth() * 2, inf.getHeight() * 2, null);
					}else{
						g.drawString(text, x + xOffset + xo, y + yOffset + yo);
					}
				}
			}
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
	}
	
	public static enum TextEffect {
        NONE, SMALL_CIRCLE, LARGE_CIRCLE, /** Demonic Flowey's voice */TWITCH, /** Battle flavor text */SHAKE, WAVE, HEAVY_WAVE, ASRIEL_DREEMURR, GASTER; 
    }

	public String getStripped() {
		
		String ret = baseString;
		ret = ret.replaceAll("\\\\.", "");
		ret = ret.replaceAll("\\�.", "");
		ret = ret.replaceAll("^", "");
		
		return ret;
	}
	
}
