package me.pieking.game;

import me.pieking.game.Game;

public class Scheduler {

	public static void delayedTask(Runnable r, int ticks){
		int start = Game.getTime();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(Game.getTime() - start < ticks){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e){}
				}
				r.run();
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	public static void repeatingTask(Runnable r, int delay, int count){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int count2 = 0;
				int last = Game.getTime();
				while(count == -1 || count2 < count){
					while(Game.getTime() - last < delay){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e){}
					}
					r.run();
					last = Game.getTime();
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
}
