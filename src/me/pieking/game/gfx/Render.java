package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Graphics2D;

import me.pieking.game.Game;

public class Render {

	public static void render(Disp d) {
		
		Graphics2D g = d.getRenderGraphics();
		
		g.clearRect(0, 0, d.getWidth(), d.getHeight());
		
		g.setColor(Color.GREEN);
		g.drawRect((int) (100 + Math.sin(Game.getTime()/10f) * 10), (int) (100 + Math.cos(Game.getTime()/10f) * 10), 20, 20);
		
	}

}
