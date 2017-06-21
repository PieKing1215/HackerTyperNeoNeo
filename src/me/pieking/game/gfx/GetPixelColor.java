package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetPixelColor {

	static List<Rectangle> rectList = new ArrayList<Rectangle>();

	// int y, x, tofind, col;
	/**
	 * @param args
	 *            the command line arguments
	 * @throws IOException
	 */
	public static List<Rectangle> get(BufferedImage img) throws IOException {
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		
		rectList.clear();
		
		BufferedImage image1 = new BufferedImage(img.getWidth() + 2, img.getHeight() + 2, img.getType());
		Graphics g = image1.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image1.getWidth(), image1.getHeight());
		g.drawImage(img, 1, 1, null);

		for (int y = 0; y < image1.getHeight(); y++) {
			for (int x = 0; x < image1.getWidth(); x++) {

				int c = image1.getRGB(x, y);
				Color color = new Color(c);
				if (color.getRed() < 30 && color.getGreen() < 30 && color.getBlue() < 30
						&& !contains(new Point(x, y))) {
					x1 = x;
					y1 = y;
					for (int i = x; i < image1.getWidth(); i++) {
						c = image1.getRGB(i, y);
						color = new Color(c);
						if (!(color.getRed() < 30 && color.getGreen() < 30 && color.getBlue() < 30)
								|| i == image1.getWidth()) {
							x2 = i;
							break;
						}
					}
					for (int i = y; i < image1.getHeight(); i++) {
						c = image1.getRGB(x, i);
						color = new Color(c);
						if (!(color.getRed() < 30 && color.getGreen() < 30 && color.getBlue() < 30)
								|| i == image1.getHeight()) {
							y2 = i;
							break;
						}
					}

					rectList.add(new Rectangle(x1 - 1, y1 - 1, (x2 - x1), (y2 - y1)));
				}
			}
		}
		System.out.println("No of rectangles = " + rectList.size());
		//printRect();
		
		return rectList;
	}

	static void printRect() {
		Rectangle r = null;
		for (int i = 0; i < rectList.size(); i++) {
			r = (Rectangle) rectList.get(i);
			System.out.println("(" + r.getMinX() + "," + r.getMinY() + ")  (" + r.getMaxX() + "," + r.getMaxY() + ")");
		}
	}

	static boolean contains(Point a) {
		Rectangle r = null;
		for (int i = 0; i < rectList.size(); i++) {
			r = (Rectangle) rectList.get(i);
			if (a.x >= r.getMinX() && a.x <= r.getMaxX() && a.y >= r.getMinY() && a.y <= r.getMaxY())
				return true;
		}
		return false;
	}
}

