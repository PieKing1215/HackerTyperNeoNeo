package me.pieking.game.gfx;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.io.output.ByteArrayOutputStream;

import me.pieking.game.Game;
import me.pieking.game.Rand;

public class Disp extends Canvas{

	private static final long serialVersionUID = 1L;

	private BufferedImage im;
	private BufferedImage corrupt;
	private int corruptTimer = 0;
	
	private int realWidth;
	private int realHeight;
	
	public Disp(int resWidth, int resHeight, int realWidth, int realHeight) {
		im = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_INT_RGB);
		this.realHeight = realHeight;
		this.realWidth = realWidth;
	}
	
	@Override
	public void paint(Graphics g) {
		
		BufferedImage img = im;
		
		if(corruptTimer > 0){
			img = corrupt;
			corruptTimer--;
		}else{
    		int glitchLevels = Game.glitchLevels;
    		
    //		System.out.println("one in " + (100 - glitchLevels));
    		if(glitchLevels > 0 && Game.getFrame().getState() != Frame.ICONIFIED){
        		if(Rand.oneIn(100 - glitchLevels)){
            		try {
            			
    //        			BufferedImage indexedImage = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
    //					Graphics2D gg = indexedImage.createGraphics();
    //					gg.drawImage(img, 0,0,null);
    //					img = indexedImage;
            			
            			ImageIO.setUseCache(false);
            			
    //        			ByteArrayOutputStream compressed = new ByteArrayOutputStream();
    //        			ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
    //
    //        			ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
    //        			ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
    //        			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    //        			jpgWriteParam.setCompressionQuality(0.5f);
    //        			jpgWriter.setOutput(outputStream);
    //        			IIOImage out = new IIOImage(img, null, null);
    ////        			long start = System.nanoTime();
    //        			jpgWriter.write(null, out, jpgWriteParam);
    ////        			System.out.println(System.nanoTime() - start);
    //        			jpgWriter.dispose();
    //
    //        			byte[] bytes = compressed.toByteArray();
            			
            			boolean doCorrupt = Rand.oneIn((100 - glitchLevels)/5);
            			
            			if(glitchLevels >= 50 && doCorrupt){
            				
            				ByteArrayOutputStream baos = new ByteArrayOutputStream(9000);
            			    
	    //        			long start = System.nanoTime();
	            			
	            			ImageIO.write(img, "jpg", baos);
	            			
	    //        			System.out.println(System.nanoTime() - start);
	            			
	            			byte[] bytes = baos.toByteArray();
            				
                			int ct = Rand.range(1, glitchLevels*2);
                			for(int i = 0; i < ct; i++){
                				int index = Rand.range(1000, bytes.length-100);
                				bytes[index]++;
                			}
                			
                			img = ImageIO.read(new ByteArrayInputStream(bytes));
            			}else{
            				img = convertImage(img);
            			}
            			
            			WritableRaster raster = img.getRaster();
            		    DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
            		    byte[] data = buffer.getData();
            		    int ct = Rand.range(1, glitchLevels*100);
            		    if(glitchLevels < 20) ct /= 2;
            			for(int i = 0; i < ct; i++){
            				data[Rand.range(100, data.length-1)] = (byte)Rand.range(0, 255);
            			}
            			
            			int maxTranslate = Math.max(1, glitchLevels/5);
            			if(glitchLevels >= 10) g.translate(Rand.range(-maxTranslate, maxTranslate), Rand.range(-maxTranslate, maxTranslate));

            			corrupt = img;
            			corruptTimer = (glitchLevels >= 50) ? 20 : 10;
            			
            			if(doCorrupt) corruptTimer += 20;
            			
            		
            		}catch (Exception e) {}
        		}
    		}
		}
		
		g.drawImage(img, 0, 0, realWidth, realHeight, null);
	}
	
	/**
	 * this method convert supplied image to suitable type
	 * it is needed because we need bytes of array so TYPE_INT images must be
	 * converted to BYTE_BGR or so
	 * @param originalImage loaded from file-chooser
	 * @return 
	 */
	public BufferedImage convertImage(BufferedImage originalImage) {
	    int newImageType = originalImage.getType();

	    /**
	     * Converting int to byte since byte array is needed later to modify 
	     * the image
	     */
	    if (newImageType == BufferedImage.TYPE_INT_RGB
	            || newImageType == BufferedImage.TYPE_INT_BGR) {
	        newImageType = BufferedImage.TYPE_3BYTE_BGR;
	    } else if (newImageType == BufferedImage.TYPE_INT_ARGB) {
	        newImageType = BufferedImage.TYPE_4BYTE_ABGR;
	    } else if (newImageType == BufferedImage.TYPE_INT_ARGB_PRE) {
	        newImageType = BufferedImage.TYPE_4BYTE_ABGR_PRE;
	    }
	    BufferedImage newImage = new BufferedImage(originalImage.getWidth(), 
	            originalImage.getHeight(), newImageType);
	    Graphics g = newImage.getGraphics();
	    g.drawImage(originalImage, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	
	public Graphics2D getRenderGraphics(){
		return (Graphics2D) im.getGraphics();
	}
	
}
