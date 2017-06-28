package me.pieking.game;

import java.util.Random;

public class Rand {

	private static long seed = System.nanoTime();
	
	private static Random random = new Random(seed);
	
	public static Random getRand(){
		//System.out.println(seed);
		return random;
	}
	
	public static int range(int min, int max){
		return getRand().nextInt((max - min) + 1) + min;
	}
	
	public static float range(float min, float max){
		return (getRand().nextFloat() * (max - min)) + min;
	}
	
	public static boolean oneIn(int x){
		if(x < 1) x = 1;
		return range(1, x) == 1;
	}
	
	public static long getSeed(){
		return seed;
	}
	
}
