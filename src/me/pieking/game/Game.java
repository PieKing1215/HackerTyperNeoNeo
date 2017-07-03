package me.pieking.game;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import me.pieking.game.command.Command;
import me.pieking.game.console.Console;
import me.pieking.game.console.TextArea;
import me.pieking.game.console.TextEditor;
import me.pieking.game.events.KeyHandler;
import me.pieking.game.events.MouseHandler;
import me.pieking.game.gfx.Disp;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.FormattedString;
import me.pieking.game.gfx.Render;
import me.pieking.game.net.Client;
import me.pieking.game.sound.Sound;

public class Game {

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	
	private static String name = "Hacker Typer Neo NEO";
	private static String version = "0.0.1-r3";
	
	private static boolean running = false;
	
	private static int fps = 0;
	private static int lastFps = 0;
	private static int tps = 0;
	
	private static JFrame frame;
	
	private static Disp disp;
	private static int time = 0;
	private static double nsPerTick = 1e9 / 60d;
	
	private static Console mainConsole;
	private static TextEditor editor;
	
	private static KeyHandler keyHandler;
	private static MouseHandler mouseHandler;
	
	public static int glitchLevels = 0;
	private static TextArea focused;
	private static int ddosLevels;
	
	public static void main(String[] args) {
//		Map charSets = Charset.availableCharsets();
//	    Iterator it = charSets.keySet().iterator();
//	    while(it.hasNext()) {
//	      String csName = (String)it.next();
//	      System.out.print(csName);
//	      Iterator aliases = ((Charset)charSets.get(csName))
//	        .aliases().iterator();
//	      if(aliases.hasNext())
//	        System.out.print(": ");
//	      while(aliases.hasNext()) {
//	        System.out.print(aliases.next());
//	        if(aliases.hasNext())
//	          System.out.print(", ");
//	      }
//	      System.out.println();
//	    }
		
//		Toolkit.getDefaultToolkit().beep();
		
//		int channel = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments
//
//	    int volume = 80; // between 0 et 127
//	    int duration = 200; // in milliseconds
//
//	    try {
//	        Synthesizer synth = MidiSystem.getSynthesizer();
//	        synth.open();
//	        MidiChannel[] channels = synth.getChannels();
//
//	        for(int i = 0; i < channels.length; i++){
//	        	MidiChannel c = channels[i];
//	        	System.out.println(i);
//	        	if(c != null){
//	        		c.noteOn( 60, volume ); // C note
//	    	        Thread.sleep( duration );
//	    	        c.noteOff( 60 );
//	    	        Thread.sleep(200);
//	        	}
//	        }
//	        
//	        Thread.sleep(500);
//	        
////	        // --------------------------------------
////	        // Play a few notes.
////	        // The two arguments to the noteOn() method are:
////	        // "MIDI note number" (pitch of the note),
////	        // and "velocity" (i.e., volume, or intensity).
////	        // Each of these arguments is between 0 and 127.
////	        channels[channel].noteOn( 60, volume ); // C note
////	        Thread.sleep( duration );
////	        channels[channel].noteOff( 60 );
////	        channels[channel].noteOn( 62, volume ); // D note
////	        Thread.sleep( duration );
////	        channels[channel].noteOff( 62 );
////	        channels[channel].noteOn( 64, volume ); // E note
////	        Thread.sleep( duration );
////	        channels[channel].noteOff( 64 );
////
////	        Thread.sleep( 500 );
////
////	        // --------------------------------------
////	        // Play a C major chord.
////	        channels[channel].noteOn( 60, volume ); // C
////	        channels[channel].noteOn( 64, volume ); // E
////	        channels[channel].noteOn( 67, volume ); // G
////	        Thread.sleep( 3000 );
////	        channels[channel].allNotesOff();
////	        Thread.sleep( 500 );
//
//
//
//	        synth.close();
//	    }
//	    catch (Exception e) {
//	        e.printStackTrace();
//	    }
		
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
				lastFps = fps;
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
		
		Sound.init();
		Sound.boot.stop();
		Sound.boot.start();
		frame.setVisible(true);
		
		
		Fonts.init();
		Command.registerDefaultCommands();
		
		mainConsole = new Console();
		mainConsole.setMaxLines(100);
		
		editor = new TextEditor();
		editor.setFile("editor.txt");

		setFocusedArea(mainConsole);
		mainConsole.startup();
		
		
//		mainConsole.setDirectory("/test");
	}
	
	private static void tick(){
		
		Client.tick();
		
//		ddosLevels = 0;
		
		if(ddosLevels > 0){
    		int lagFactor = 110 - ddosLevels;
    		
    		if(Rand.oneIn(110 - ddosLevels)){
    			int delayLevels = ddosLevels * 2;
    			
    			int minDelay = delayLevels - 20;
    			if(minDelay < 10) minDelay = 10;
    			
    			int maxDelay = delayLevels + 10;
    			if(maxDelay < 20) maxDelay = 20;
    			try {
        			Thread.sleep(Rand.range(minDelay, maxDelay));
        		}catch (InterruptedException e) {
        			e.printStackTrace();
        		}
    		}
    		
    		if(Rand.oneIn(110 - ddosLevels)) lagFactor = 10;
    		
    		if(lagFactor < 10) lagFactor = 10;
		
    		if(fps > 20){
    			long delay = fps/lagFactor;
        		try {
        			Thread.sleep(delay);
        		}catch (InterruptedException e) {
        			e.printStackTrace();
        		}
    		}else if(lastFps > 20){
    			long delay = lastFps/lagFactor;
        		try {
        			Thread.sleep(delay);
        		}catch (InterruptedException e) {
        			e.printStackTrace();
        		}
    		}
		}
		
		if(Game.getTime() % 10 == 0){
    		String title = name + " v" + version + " | " + fps + " FPS " + tps + " TPS";
    		
    		if(glitchLevels > 20){
        		byte[] b = title.getBytes();
        		
        		int ct = Rand.range(0, (b.length-1)/2);
        		for(int i = 0; i < ct; i++){
        			if(Rand.oneIn((100-glitchLevels)/4)) b[Rand.range(0, (b.length-1))]++;
        		}
        		
        		title = new String(b);
    		}
    		
    		frame.setTitle(title);
		}
		
//		glitchLevels = 0;

//		if(Game.getTime() % 60 == 0) {
//			glitchLevels++;
//			System.out.println(glitchLevels);
//		}
		
		int maxTranslate = Math.max(1, glitchLevels/5);
		if(glitchLevels >= 10 && Game.getTime() % 10 == 0 && Rand.oneIn((100 - glitchLevels)/6)){
			int xa = Rand.range(-maxTranslate, maxTranslate);
			int ya = Rand.range(-maxTranslate, maxTranslate);
			Point oldLoc = frame.getLocation();
			frame.setLocation(frame.getLocation().x + xa, frame.getLocation().y + ya);
			Scheduler.delayedTask(() -> {
				frame.setLocation(oldLoc);
			}, 5);
		}
		
		FormattedString.updateRand();
		
		mainConsole.tick();
		getFocusedArea().tick();
		
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
	
	public static TextArea getFocusedArea() {
		return focused;
	}
	
	public static void setFocusedArea(TextArea console) {
		focused = console;
	}
	
	public static KeyHandler keyHandler(){
		return keyHandler;
	}
	
	public static MouseHandler mouseHandler(){
		return mouseHandler;
	}

	
	private static File baseDir = null;
	
	public static File getFileDir(){
		
		if(baseDir != null) return baseDir;
		
		if(ranFromJar()){
			try {
				
				ProtectionDomain pd = Game.class.getProtectionDomain();
				CodeSource cs = pd.getCodeSource();
				URL l = cs.getLocation();
				URI i = l.toURI();
				String p = i.getPath();
				
//				System.out.println(pd + "|" + cs + "|" + l + "|" + i + "|" + p);
				
				File runLoc = new File(p);
//				System.out.println(runLoc.getCanonicalPath());
				File f = new File(runLoc.getParentFile(), "/filesystem");
				f.mkdir();
				return f;
			}catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
			
		File f = new File(getDesktop(), "/hackertyperneoneo/filesystem");
		f.mkdir();
		return f;
	}
	
	public static File getDesktop(){
		return FileSystemView.getFileSystemView().getHomeDirectory();
	}
	
	/**
	 * Credit to <a href="https://stackoverflow.com/a/482566">https://stackoverflow.com/a/482566</a>.
	 */
	public static boolean ranFromJar(){
		String res = Game.class.getResource("Game.class").toString();
//		System.out.println(res);
		return res.startsWith("jar:") || res.startsWith("rsrc:");
	}

	public static void reboot() {
		mainConsole.canInput = false;
		mainConsole.clear();
		mainConsole.setRunning(null);
		Sound.boot.stop();
		Sound.boot.start();
		mainConsole.startup();
	}

	public static void edit(String relativePath) {
		editor.setFile(relativePath);
		setFocusedArea(editor);
	}
	
}
