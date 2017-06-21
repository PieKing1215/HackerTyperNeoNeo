package me.pieking.game.sound;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;
import me.pieking.game.Logger;
import me.pieking.game.Resources;

/**
 * Wraps the <a href="https://github.com/finnkuusisto/TinySound/">TinySound</a> API.
 */
public class TinySoundClip extends SoundClip{

	public Music baseClip;
	
	public TinySoundClip(Music baseClip, String name, float volume, boolean loop, boolean priority) {
		this(baseClip, name, volume, loop, priority, true);
	}
	
	public TinySoundClip(Music baseClip, String name, float volume, boolean loop, boolean priority, boolean playNow){
		super(name, volume, loop, priority);
		
		baseClip.setLoop(loop);
		baseClip.setLoopPositionByFrame(0);
		
		float vol = 1f + (volume - 0.5f);
		
		//System.out.println(name + " " + vol + " * " + Sound.soundSystem.masterVolume);
		
		baseClip.setVolume(vol * Sound.soundSystem.masterVolume);
		
		this.baseClip = baseClip;
		if(playNow){
			if(loop){
				loop();
			}else{
				start();
			}
		}
	}

	@Override
	public void close() {
		try{
			baseClip.unload();
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to unload() on " + getName() + ": " + e.getMessage());
			//e.printStackTrace();
		}
	}

	@Override
	public void pause() {
		//System.out.println("before pause: " + baseClip.loop());
		try{
			baseClip.pause();
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to pause() on " + getName() + ":");
			e.printStackTrace();
		}
		//System.out.println("after pause: " + baseClip.loop());
	}

	@Override
	public void unpause() {
		//System.out.println("before unpause: " + baseClip.loop());
		try{
			baseClip.resume();
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to resume() on " + getName() + ":");
			e.printStackTrace();
		}
		//System.out.println("after unpause: " + baseClip.loop());
	}

	@Override
	public void stop() {
		System.out.println("stop " + getName());
		try{
			baseClip.stop();
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to stop() on " + getName() + ":");
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		System.out.println("start " + getName());
		try{
			baseClip.setLoop(false);
			baseClip.play(false);
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to start() on " + getName() + ":");
			e.printStackTrace();
		}
	}

	@Override
	public void setLoop(boolean loop) {
		try{
			baseClip.setLoop(loop);
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to setLoop() on " + getName() + ":");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean getLoop(){
		try{
			return baseClip.loop();
		}catch(Exception e){
			Logger.warn("A Tiny Sound internal error occurred trying to getLoop() on " + getName() + ":");
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void loop() {
		try{
			baseClip.setLoop(true);
			baseClip.play(true);
		}catch(NullPointerException e){
			Logger.warn("A Tiny Sound internal error occurred trying to loop() on " + getName() + ":");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isPlaying() {
		return baseClip.playing(); //Not sure whether to use done() or playing(). The docs make them seem the same.
	}
	
	@Override
	public void setVolume(float volume) {
		try{
			volume = volume * Sound.soundSystem.masterVolume;
			baseClip.setVolume((double) volume);
			super.setVolume(volume);
		}catch(Exception e){
			
		}
	}
	
	@Override
	public void setAbsoluteVolume(float volume) {
		try{
			baseClip.setVolume((double) volume);
			super.setAbsoluteVolume(volume);
		}catch(Exception e){
			
		}
	}
	
	@Override
	public float getVolume() {
		return (float) baseClip.getVolume();
	}

	@Override
	public void reload() {
		URL play;
		try {
			Music oldClip = baseClip;
			play = Resources.getSound(getName()).toURI().toURL();
			baseClip = TinySound.loadMusic(play, true);
			baseClip.setLoop(oldClip.loop());
			//System.out.println(getName() + " " + oldClip.loop());
			baseClip.setLoopPositionByFrame(oldClip.getLoopPositionByFrame());
			baseClip.setVolume(oldClip.getVolume());
			baseClip.setPan(oldClip.getPan());
			if(oldClip.playing()){
				baseClip.play(oldClip.loop());
			}
			oldClip.stop();
			oldClip.unload();
		} catch (MalformedURLException | URISyntaxException e) {
			Logger.warn("Could not reload sound: " + getName());
			e.printStackTrace();
		}
	}

	@Override
	public boolean exists(){
		try{
			baseClip.done();
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	@Override
	public boolean setLoopPosition(float f) {
		try{
			baseClip.setLoopPositionBySeconds(f);
			return true;
		}catch(Exception e){
			return false;
		}
		
	}

}
