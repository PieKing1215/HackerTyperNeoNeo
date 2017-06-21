package me.pieking.game.gfx;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class Fonts {

	public static Font wingDings = new Font("WingDings", 0, 16);
	
	public static void init(){
		//ex: damage = loadFont("damage.ttf");
	}
	
	public static Font loadFont(String fileName){
		InputStream is = Fonts.class.getResourceAsStream("/fonts/" + fileName);
		
		try {
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			return font2.deriveFont(0, 20);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new Font("Arial", 0, 20);
	}
	
	public static String execFormatValue(String str, String f) {

	  // Idk what this means

	  // Gewisse Schriften können die alten Texte nicht anzeigen.
	  // Beispielsweise sollte "A" für WingDings ein Victory Zeichen zeigen.
	  // Dies funktioniert neuerdings nur, wenn man in der Private Use Area nachschaut.
	  // http://www4.carthage.edu/faculty/ewheeler/GrafiX/LessonsAdvanced/wingdings.pdf
	  // http://www.fileformat.info/info/unicode/char/270c/index.htm
	  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6176474
		if (str != null && f != null) {
			Font font = new Font(f, Font.PLAIN, 1);
			boolean changed = false;
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (!font.canDisplay(chars[i])) {
					if (chars[i] < 0xF000) {
						chars[i] += 0xF000;
						changed = true;
					}
				}
			}
			if (changed)
				str = new String(chars);
		}
		return str;
	}
	
	public static String execFormatValue(String str, Font f) {

		  // Idk what this means

		  // Gewisse Schriften können die alten Texte nicht anzeigen.
		  // Beispielsweise sollte "A" für WingDings ein Victory Zeichen zeigen.
		  // Dies funktioniert neuerdings nur, wenn man in der Private Use Area nachschaut.
		  // http://www4.carthage.edu/faculty/ewheeler/GrafiX/LessonsAdvanced/wingdings.pdf
		  // http://www.fileformat.info/info/unicode/char/270c/index.htm
		  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6176474
			if (str != null && f != null) {
				Font font = f;
				boolean changed = false;
				char[] chars = str.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					if (!font.canDisplay(chars[i])) {
						if (chars[i] < 0xF000) {
							chars[i] += 0xF000;
							changed = true;
						}
					}
				}
				if (changed)
					str = new String(chars);
			}
			return str;
		}
	
}
