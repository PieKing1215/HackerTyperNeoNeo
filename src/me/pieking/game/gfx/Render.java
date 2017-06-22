package me.pieking.game.gfx;

import java.awt.Graphics2D;

import me.pieking.game.Game;

public class Render {

	@SuppressWarnings("unused")
	private static Sprite testSpr = Images.getSprite("test.png");
	@SuppressWarnings("unused")
	private static AnimatedImage testAnim = new AnimatedImage("testAnim_", 4);
	
	public static void render(Disp d) {
		Graphics2D g = d.getRenderGraphics();
		
		g.clearRect(0, 0, d.getWidth(), d.getHeight());
		
		Game.getMainConsole().render(g);
	}

}
