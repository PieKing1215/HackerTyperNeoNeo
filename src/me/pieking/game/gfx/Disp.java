package me.pieking.game.gfx;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Disp extends Canvas{

	private static final long serialVersionUID = 1L;

	private BufferedImage im;
	
	private int realWidth;
	private int realHeight;
	
	public Disp(int resWidth, int resHeight, int realWidth, int realHeight) {
		im = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_INT_RGB);
		this.realHeight = realHeight;
		this.realWidth = realWidth;
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(im, 0, 0, realWidth, realHeight, null);
	}
	
	public Graphics2D getRenderGraphics(){
		return (Graphics2D) im.getGraphics();
	}
	
}
