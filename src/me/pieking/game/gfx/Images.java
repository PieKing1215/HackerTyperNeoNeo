package me.pieking.game.gfx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

import me.pieking.game.Logger;

public class Images extends Thread{

	public static final Image error = getImage("error.png");
	public static final Sprite errorS = getSprite("error.png");
	public static final Image nothing = getImage("nothing.png");
	public static final Sprite nothingS = getSprite("nothing.png");
	
	public static Sprite getSprite(String name){
		return new Sprite(name);
	}
	
	public static Sprite getSpritePath(String name){
		return new Sprite(name, false);
	}
	
	/**
	 * <b>Replaced by {@link #getSprite()}.</b><br>
	 */
	@Deprecated
	public static Image getImage(String name){
		//System.out.println("getImage(" + name + ");");
		//Game.debug(DebugLevel.INFO, DebugPriority.DEV, "Loading image: " + name);
		Logger.info("Loading image: " + name, Logger.VB_DEV_ONLY);
		
		Image img=null;
		if(!name.equals("error.png")) {
			img = error;
		}
		try{
			img = new ImageIcon(getTexture(name)).getImage();
		}catch(NullPointerException e){
			//Game.debug(DebugLevel.WARNING, DebugPriority.NORMAL, "Texture not found: textures/"+name);
			Logger.warn("Texture not found: textures/" + name);
			if(name.equals("error.png"))img = new ImageIcon(getTexture("error.png")).getImage();
		}
		
		//Game.debug(DebugLevel.INFO, DebugPriority.DEV, "Done loading image: " + name);
		Logger.info("Done loading image: " + name, Logger.VB_DEV_ONLY);
		
		return img;
	}
	
	public static URL getTexture(String path){
		return getResource("textures/" + path);
	}
	
	public static URL getResource(String path){
		URL ret = Images.class.getClassLoader().getResource("" + path);
		return ret;
	}

	/**
	 * <b>Replaced by {@link #getSpritePath()}.</b><br>
	 */
	@Deprecated
	public static Image getImagePath(String name){
		//System.out.println("getImagePath(" + name + ");");
		Image img=null;
		if(!name.equals("error.png")) {
			img = error;
		}
		try{
			try{
				img = new ImageIcon(new URL(name).toURI().toURL()).getImage();	
				System.out.println("load " + img.getWidth(null) + " " + img.getHeight(null));
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(NullPointerException e){
			//Game.debug(DebugLevel.WARNING, DebugPriority.NORMAL, "Texture not found: res/"+name);
			Logger.warn("Texture not found: res/"+name);
			
			if(name.equals("error.png"))img = new ImageIcon(Images.class.getClassLoader().getResource("textures/error.png")).getImage();
		}
		
		return img;
	}
	
	public static BufferedImage toBufferedImage(Image img){
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
//	/**
//	 * Doesn't support translucency.
//	 */
//	public static BufferedImage getImageSilhouette(Image img, float darkness){
//		boolean bad = false;
//		
//		BufferedImage b = toBufferedImage(img);
//		
//		if(Combat.currCombat.containsMonster("Photoshop Flowey")){
//			if(((FloweyX) Combat.currCombat.getMonster("Photoshop Flowey")).phase == 3){
//				if(darkness == 1) return toBufferedImage(nothing);
//			}
//		}
//		
//		if(bad || darkness == 0) return b;
//		
//		HSBAdjustFilter f = new HSBAdjustFilter();
//		f.setBFactor(-darkness);
//		
//		f.filter(b, b);
//		
//		return b;
//	}
	
	/*public static BufferedImage IplImageToBufferedImage(IplImage src) { //Back when I didnt realize having the 150MB javaCv jars was a problem for hosting the game on a 500MB limit
	    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	    Frame frame = grabberConverter.convert(src);
	    return paintConverter.getBufferedImage(frame,1);
	}*/
	
}
