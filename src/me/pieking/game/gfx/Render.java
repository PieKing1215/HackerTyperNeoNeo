package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import me.pieking.game.Game;
import me.pieking.game.Utils;

public class Render {

	private static Sprite testSpr = Images.getSprite("test.png");
	private static AnimatedImage testAnim = new AnimatedImage("testAnim_", 4);
	
	public static void render(Disp d) {
		
		Graphics2D g = d.getRenderGraphics();
		
		g.clearRect(0, 0, d.getWidth(), d.getHeight());
		
		g.setColor(Color.GREEN);
		g.drawRect((int) (100 + Math.sin(Game.getTime()/10f) * 10), (int) (100 + Math.cos(Game.getTime()/10f) * 10), 20, 20);
		
		Sprite animFrame = testAnim.getFrameSprite((Game.getTime()/10) % 4);
		g.drawImage(animFrame.getImage(), 200, 100, animFrame.getWidth() * 2, animFrame.getHeight() * 2, null);
		
		
		AffineTransform trans = g.getTransform();
		
		g.rotate(Math.toRadians(Math.sin(Game.getTime() / 20f))*8f, 250, 250);
		int animWidth = (int) (testSpr.getWidth() + ((Math.sin(Game.getTime() / 10f) + 1) * 20));
		int animHeight = (int) (testSpr.getHeight() + ((Math.cos(Game.getTime() / 10f) + 1) * 20));
		g.drawImage(testSpr.getImage(), 250 - animWidth/2, 250 - animHeight/2, animWidth, animHeight, null);
		
		g.setTransform(trans);
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.anonymous.deriveFont(20f));
		g.drawString("Hello!", 400, 120);
		
		g.setColor(Color.CYAN);
		g.setFont(Fonts.pixeled.deriveFont(20f));
		g.drawString("Hello!", 400, 155);
		
		g.setColor(Utils.rainbowColor(0.1f, 0));
		g.setFont(Fonts.threeDventure.deriveFont(20f));
		g.drawString("Hello!", 400, 180);
		
	}

}
