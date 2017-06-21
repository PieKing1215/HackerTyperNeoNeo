package me.pieking.game;

public class Location{

	public float x,y;
	
	public Location(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return Math.round(x);
	}
	
	public int getY(){
		return Math.round(y);
	}
	
	public int getTileX(){
		return getX() >> 3;
	}
	
	public int getTileY(){
		return getY() >> 3;
	}
	
	public double getDistance(Location l){
		double dist = Math.sqrt(Math.pow((l.getX()-getX()), 2)+Math.pow((l.getY()-getY()), 2));
		return Math.abs(dist);
	}
	
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public void set(Location loc){
		this.x = loc.x;
		this.y = loc.y;
	}
	
	public Location add(float x, float y){
		return new Location(this.x + x, this.y + y);
	}
	
	public void move(float x, float y){
		this.x += x;
		this.y += y;
	}
	
	public Location clone(){
		return add(0, 0);
	}
	
}
