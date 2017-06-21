package me.pieking.game.sound;

import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Logger;

public abstract class SoundSystem {

	protected static SoundClip test;
	
	public SoundSystem(){
		setVolume(masterVolume);
	}
	
	/**
	 * The clips used by the sound system.
	 */
	public List<SoundClip> clips = new ArrayList<SoundClip>();
	
	/**
	 * The master volume for the sound system. <br>Works like this: <code>volume = clipVolume * masterVolume</code>
	 */
	//Don't change here, it's set in Sound.init()
	public float masterVolume;

	/**
	 * Timer for the volume display.
	 */
	public int volumeTimer = 0;
	
	/**
	 * Initiates the sound system, if necessary.
	 */
	public abstract void init();
	
	/**
	 * Play a sound with the given name, without loop.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 * @param volume The volume of the sound, from 0.0 to 1.0. Any invalid values will be coerced.
	 */
	public SoundClip playSound(String name, float volume){
		return playSoundSemiFinal(name, volume, false);
	}
	
	/**
	 * Play a loop of a sound with the given name.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 * @param volume The volume of the sound, from 0.0 to 1.0. Any invalid values will be coerced.
	 */
	public SoundClip playSoundLoop(String name, float volume){
		return playSoundSemiFinal(name, volume, true);
	}
	
	/**
	 * Play a sound with the given name.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 * @param volume The volume of the sound, from 0.0 to 1.0. Any invalid values will be coerced.
	 * @param loop Whether to loop the sound, or not.
	 */
	public SoundClip playSoundSemiFinal(String name, float volume, boolean loop){
		if(name.equals("null") || name.equals("none")) return null;
		return playSoundFinal(name, volume, loop, false);
	}
	
	/**
	 * Play a sound with the given name.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 * @param volume The volume of the sound, from 0.0 to 1.0. Any invalid values will be coerced.
	 * @param loop Whether to loop the sound, or not.
	 * @param priority Whether the sound should ignore stopSounds(). Will still be stopped by hardStopSounds().
	 */
	public abstract SoundClip playSoundFinal(String name, float volume, boolean loop, boolean priority);
	
	/**
	 * Stops all non-priority sounds.
	 * 
	 * @param closeSounds If true, the sounds will be closed (and thus unusable), too.
	 */
	public void stopSounds(boolean closeSounds){
		if(closeSounds){
			List<SoundClip> toRemove = new ArrayList<SoundClip>();
			
			List<SoundClip> clippys = new ArrayList<SoundClip>();
			clippys.addAll(clips);
			
			for(SoundClip clip : clippys){
				if(!clip.hasPriority()){
					clip.stop();
					clip.close();
					
					toRemove.add(clip);
					//System.out.println("stopped " + clip.getName());
				}
			}
			
			clips.removeAll(toRemove);
		}else{
			List<SoundClip> clippys = new ArrayList<SoundClip>();
			clippys.addAll(clips);
			
			for(SoundClip clip : clippys){
				if(!clip.hasPriority()){
					clip.stop();
				}
			}
		}
	}
	
	
	/**
	 * Stops ALL sounds, including priority sounds.
	 * 
	 * @param closeSounds If true, the sounds will be closed (and thus unusable), too.
	 */
	public void hardStopSounds(boolean closeSounds){
		if(closeSounds){
			List<SoundClip> clipsToRemove = new ArrayList<SoundClip>();
			List<SoundClip> clippys = new ArrayList<SoundClip>();
			clippys.addAll(clips);
			
			for(SoundClip clip : clippys){
				clip.stop();
				clip.close();
				clipsToRemove.add(clip);
			}
			
			clips.removeAll(clipsToRemove);
		}else{
			List<SoundClip> clippys = new ArrayList<SoundClip>();
			clippys.addAll(clips);
			
			for(SoundClip clip : clippys){
				clip.stop();
			}
		}
	}
	
	/**
	 * Stops ALL sounds if they are playing, including priority sounds.
	 */
	public void hardStopPlaying(){
		List<SoundClip> clipsToRemove = new ArrayList<SoundClip>();
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			if(clip.isPlaying()){
				clip.stop();
				clip.close();
				clipsToRemove.add(clip);
			}
		}
		
		clips.removeAll(clipsToRemove);
	}
	
	/**
	 * Stops the sound with the given name. Will also stop priority sounds.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public void stopSound(String name){
		List<SoundClip> toRemove = new ArrayList<SoundClip>();
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			if(clip.getName().equals(name)){
				clip.stop();
				clip.close();
				
				toRemove.add(clip);
			}
		}
		
		clips.removeAll(toRemove);
	}
	
	/**
	 * Pause all sounds, including priority sounds.
	 */
	public void pause(){
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			clip.pause();
		}
	}
	
	/**
	 * Unpause all sounds, including priority sounds.
	 */
	public void unpause(){
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			//System.out.println(clip.getName());
			clip.unpause();
		}
	}
	
	/**
	 * Pauses the sound with the given name. Will also pause priority sounds.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public void pause(String name){
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			if(clip.getName().equals(name)){
				clip.pause();
			}
		}
	}
	
	/**
	 * Unpauses the sound with the given name. Will also pause priority sounds.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public void unpause(String name){
		List<SoundClip> clippys = new ArrayList<SoundClip>();
		clippys.addAll(clips);
		
		for(SoundClip clip : clippys){
			if(clip.getName().equals(name)){
				clip.unpause();
			}
		}
	}
	
	/**
	 * Set the master volume.
	 * 
	 * @param volume The master volume for the sound system. <br>Works like this: <i>volume = clipVolume * masterVolume</i>
	 */
	public void setVolume(float volume){
		this.masterVolume = volume;
		//System.out.println(masterVolume);
		this.volumeTimer = 60 * 3;
		//playSound("SFX/Misc/bullet warning.ogg", 0f);
	}
	
	/**
	 * @return The master volume for the sound system. <br>Works like this: <i>volume = clipVolume * masterVolume</i>
	 */
	public float getVolume(){
		return masterVolume;
	}
	
	/**
	 * Loads the sound with the given name into memory, if available. Check for availability with {@link #canLoadSounds()}.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 * @return 
	 */
	public abstract SoundClip loadSound(String name);
	
	/**
	 * @return <b>true</b> if this sound system supports loading sounds, <b>false</b> if not.
	 */
	public abstract boolean canLoadSounds();
	
	/**
	 * Handles any periodic things the sound system might want to do (release resources, update status, etc.). Very sound system dependent. <br>Called every 600 ticks (10 seconds)
	 */
	public abstract void periodic();
	
	/**
	 * Same as {@link #periodic()}, but is called every tick (60 ticks per second)
	 */
	public void tick() {
		if(volumeTimer > 0){
			volumeTimer--;
		}
	}
	
	/**
	 * Shuts down the sound system, if necessary.
	 */
	public abstract void shutdown();
	
	/**
	 * Reloads all sounds.
	 */
	public void reload(){
		Logger.info("Reloading sounds...");
		
		List<SoundClip> cs = new ArrayList<SoundClip>();
		cs.addAll(clips);

		for(SoundClip c : cs){
			c.reload();
		}
		
		Logger.info("Reloaded sounds!");
	}
	
}
