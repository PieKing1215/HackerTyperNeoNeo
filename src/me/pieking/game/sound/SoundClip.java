package me.pieking.game.sound;

public abstract class SoundClip {

	/**
	 * The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	private String name;
	
	/**
	 * The volume of the sound, from 0.0 to 1.0.
	 */
	private float volume = 1f;
	
	/**
	 * Whether the sound should loop.
	 */
	private boolean loop;
	
	/**
	 * Whether the sound has priority, meaning it won't stop with SoundSystem.stopSounds(). SoundSystem.hardStopSounds() will still work, however.
	 */
	private boolean priority = false;
	
	/**
	 * Creates a sound clip with the given name.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public SoundClip(String name, float volume, boolean loop, boolean priority){
		this.name = name;
		this.volume = volume;
		this.loop = loop;
		this.priority = priority;
	}
	
	/**
	 * @return The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return Whether the sound should loop.
	 */
	public boolean getLoop(){
		return loop;
	}
	
	/**
	 * @return Whether the sound has priority, meaning it won't stop with SoundSystem.stopSounds(). SoundSystem.hardStopSounds() will still work, however.
	 */
	public boolean hasPriority(){
		return priority;
	}
	
	/**
	 * Set the filepath of the sound.
	 * 
	 * @param name The filepath of the sound, extending from res/sound/.<br>Ex: "Music/Mus_flowey.ogg"
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * @return The volume of the sound, from 0.0 to 1.0.
	 */
	public float getVolume(){
		return volume;
	}
	
	/**
	 * Set the volume of the sound.<br>
	 * <b>volume</b> is multiplied by the masterVolume automatically.
	 * 
	 * @param volume The volume of the sound, from 0.0 to 1.0.
	 */
	public void setVolume(float volume){
		this.volume = volume * Sound.soundSystem.masterVolume;
	}
	
	/**
	 * Set the volume of the sound.<br>
	 * <b>volume</b> is <b>NOT</b> multiplied by the masterVolume automatically.
	 * 
	 * @param volume The volume of the sound, from 0.0 to 1.0.
	 */
	public void setAbsoluteVolume(float volume){
		this.volume = volume;
	}
	
	/**
	 * Set whether the sound should loop.
	 * 
	 * @param loop Whether the sound should loop.
	 */
	public void setLoop(boolean loop){
		this.loop = loop;
	}
	
	/**
	 * Set the sound's priority.
	 * 
	 * @param priority Whether the sound has priority, meaning it won't stop with SoundSystem.stopSounds(). SoundSystem.hardStopSounds() will still work, however.
	 */
	public void setPriority(boolean priority){
		this.priority = priority;
	}

	/**
	 * Release any resources from memory.
	 */
	public abstract void close();
	
	/**
	 * Pause the sound.
	 */
	public abstract void pause();
	
	/**
	 * Unpause the sound.
	 */
	public abstract void unpause();
	
	/**
	 * Stop the sound.
	 */
	public abstract void stop();
	
	/**
	 * Play the sound.
	 */
	public abstract void start();
	
	/**
	 * Play and loop the sound.
	 */
	public abstract void loop();
	
	/**
	 * @return Whether the sound is playing.
	 */
	public abstract boolean isPlaying();
	
	/**
	 * Reload the sound.
	 */
	public abstract void reload();

	/**
	 * @return <b>true</b> if the sound is usable, <b>false</b> if not.
	 */
	public abstract boolean exists();

	public abstract boolean setLoopPosition(float f);
	
}
