package me.pieking.game.sound;

import java.util.List;
import java.util.Random;

public class RandomSoundClip extends SoundClip{

	public List<SoundClip> clips;
	
	public RandomSoundClip(List<SoundClip> sounds) {
		super("randomClip", 0f, false, false);
		
		this.clips = sounds;
	}

	@Override
	public void close() {
		for(SoundClip c : clips){
			c.close();
		}
	}

	@Override
	public void pause() {
		for(SoundClip c : clips){
			c.pause();
		}
	}

	@Override
	public void unpause() {
		for(SoundClip c : clips){
			c.unpause();
		}
	}

	@Override
	public void stop() {
		for(SoundClip c : clips){
			c.stop();
		}
	}

	@Override
	public void start() {
		for(SoundClip c : clips){
			c.stop();
		}
		//SoundClip c = randomClip();
		//System.out.println(c + " " + c.getName());
		randomClip().start();
	}

	@Override
	public void loop() {
		for(SoundClip c : clips){
			c.stop();
		}
		randomClip().loop();
	}

	@Override
	public boolean isPlaying() {
		for(SoundClip c : clips){
			if(c.isPlaying()) return true;
		}
		return false;
	}

	@Override
	public void reload() {
		for(SoundClip c : clips){
			c.reload();
		}
	}

	@Override
	public boolean exists() {
		for(SoundClip c : clips){
			if(!c.exists()) return false;
		}
		return true;
	}

	public SoundClip randomClip(){
		return clips.get(new Random().nextInt(clips.size()));
	}
	
	@Override
	public void setVolume(float volume) {
		super.setVolume(volume);
		
		for(SoundClip c : clips){
			c.setVolume(volume);
		}
	}

	@Override
	public boolean setLoopPosition(float f) {
		return false;
	}
	
}
