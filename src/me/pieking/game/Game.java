package me.pieking.game;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.pieking.game.command.Command;
import me.pieking.game.console.Console;
import me.pieking.game.events.KeyHandler;
import me.pieking.game.events.MouseHandler;
import me.pieking.game.gfx.Disp;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Render;
import me.pieking.game.sound.Sound;

public class Game {

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	
	private static String name = "Hacker Typer Neo NEO";
	private static String version = "0.0.0-r1";
	
	private static boolean running = false;
	
	private static int fps = 0;
	private static int tps = 0;
	
	private static JFrame frame;
	
	private static Disp disp;
	private static int time = 0;
	private static double nsPerTick = 1e9 / 60d;
	
	private static Console mainConsole;
	
	private static KeyHandler keyHandler;
	private static MouseHandler mouseHandler;
	
	public static void main(String[] args) {
		run();
	}

	private static void run(){
		init();
		
		long last = System.nanoTime();
		long now = System.nanoTime();
		
		double delta = 0d;
		
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

		keyHandler = new KeyHandler();
		disp.addKeyListener(keyHandler);
		
		mouseHandler = new MouseHandler();
		disp.addMouseListener(mouseHandler);
		disp.addMouseWheelListener(mouseHandler);
		
		frame.add(disp);
		
		frame.setVisible(true);
		
		Sound.init();
		Fonts.init();
		Command.registerDefaultCommands();
		
		mainConsole = new Console();
		mainConsole.setMaxLines(64);
	}
	
	private static void tick(){
		frame.setTitle(name + " v" + version + " | " + fps + " FPS " + tps + " TPS");
		
//		if(time % 120 == 0){
//			mainConsole.write(time + "");
//		}
		
		mainConsole.tick();
		
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
		return time;
	}
	
	public static void stop(int status){
		System.exit(status);
	}
	
	public static int getWidth(){
		return WIDTH;
	}
	
	public static int getHeight(){
		return HEIGHT;
	}
	
	public static Disp getDisp(){
		return disp;
	}
	
	public static JFrame getFrame(){
		return frame;
	}
	
	public static void setTPS(int tps){
		nsPerTick = 1e9 / (double)tps;
	}
	
	public static double getTPS(){
		return 1e9 / nsPerTick;
	}
	
	public static Console getMainConsole(){
		return mainConsole;
	}
	
	public static KeyHandler keyHandler(){
		return keyHandler;
	}
	
	public static MouseHandler mouseHandler(){
		return mouseHandler;
	}

	public static Console focusedConsole() {
		return getMainConsole();
	}
	
}
