package me.pieking.game;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.pieking.game.gfx.Disp;
import me.pieking.game.gfx.Render;

public class Game {

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	
	private static String name = "GameTemplate";
	private static String version = "0.0.0-r0";
	
	private static boolean running = false;
	
	private static int fps = 0;
	private static int tps = 0;
	
	private static JFrame frame;
	
	private static Disp disp;
	private static int time = 0;
	
	public static void main(String[] args) {
		run();
	}

	private static void run(){
		init();
		
		long last = System.nanoTime();
		long now = System.nanoTime();
		
		double delta = 0d;
		
		double nsPerTick = 1e9 / 60d;
		
		long timer = System.currentTimeMillis();
		
		int frames = 0;
		int ticks = 0;
		
		running = true;
		
		while(running){
			now = System.nanoTime();
			
			long diff = now - last;
			
			delta += diff / nsPerTick;
			
			boolean shouldRender = true;
			
			while(delta >= 1){
				delta--;
				tick();
				ticks++;
				shouldRender = true;
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
			
			if(shouldRender){
				render();
				frames++;
			}
			
			last = now;
			
			if(System.currentTimeMillis() - timer >= 1000){
				timer = System.currentTimeMillis();
				fps = frames;
				tps = ticks;
				frames = 0;
				ticks = 0;
			}
			
			
		}
		
	}
	
	private static void init(){
		frame = new JFrame(name + " v" + version + " | " + fps + " FPS " + tps + " TPS");
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.add(jp);
		frame.pack();
		
		jp.setVisible(false);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		disp = new Disp(WIDTH, HEIGHT, WIDTH, HEIGHT);
		
		frame.add(disp);
		
		frame.setVisible(true);
	}
	
	private static void tick(){
		//System.out.println(fps + " " + tps);
		frame.setTitle(name + " v" + version + " | " + fps + " FPS " + tps + " TPS");
		
		time++;
	}
	
	private static void render(){
		Render.render(disp);
		disp.paint(disp.getGraphics());
	}
	
	public static String getName(){
		return name;
	}

	public static String getVersion(){
		return version;
	}

	public static int getTime() {
		return time ;
	}
	
	public static void stop(int status){
		System.exit(status);
	}
	
}
