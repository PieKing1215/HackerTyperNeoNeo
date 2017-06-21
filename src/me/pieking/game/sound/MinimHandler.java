package me.pieking.game.sound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MinimHandler {

	public String sketchPath(String fileName){
		return fileName;
	}
	
	public InputStream createInput(String fileName){
		try {
			return new URL(fileName).openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
