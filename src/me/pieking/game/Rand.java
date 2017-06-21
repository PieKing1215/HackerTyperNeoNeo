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
		int r = getRand().nextInt((x - 0) + 1) + 0;
		if(r==0){
			return true;
		}else{
			return false;
		}
	}
	
	public static long getSeed(){
		return seed;
	}
	
}
