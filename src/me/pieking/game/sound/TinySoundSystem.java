package me.pieking.game.sound;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;
import me.pieking.game.Logger;
import me.pieking.game.Resources;

public class TinySoundSystem extends SoundSystem{

	@Override
	public void init() {
		TinySound.init();
		Logger.info("Started TinySound System!");
	}

	@Override
	public TinySoundClip playSoundFinal(String name, float volume, boolean loop, boolean priority) {
		if(!TinySound.isInitialized()){
			Logger.warn("TinySound not initialized!");
			return null;
		}
		URL play = Resources.getSound(name);
		//System.out.println(play);
		TinySoundClip clip = new TinySoundClip(TinySound.loadMusic(play, true), name, volume, loop, priority);
		clips.add(clip);
		
		return clip;
	}

	@Override
	public SoundClip loadSound(String name) {
		URL play;
		try {
			URL a = Resources.getSound(name);
			//System.out.println("Resources.getSound(" + name + ") == " + a);
			play = a.toURI().toURL();
			//System.out.println("a.toURI().toURL() == " + play);
			//System.out.println("play = " + play.toString());
			Music baseClip = TinySound.loadMusic(play, true);
			TinySoundClip clip = new TinySoundClip(baseClip, name, 0.5f, false, false, false);
			clips.add(clip);
			return clip;
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean canLoadSounds() {
		return true;
	}

	@Override
	public void periodic() {
		/*for(SoundClip sc : clips){
			TinySoundClip clip = (TinySoundClip) sc;
			
			if(clip.baseClip.done()){
				clip.stop();
				clip.close();
			}
		}*/
	}

	@Override
	public void shutdown() {
		TinySound.shutdown();
	}

	@Override
	public void setVolume(float volume) {
		if(volume < 0) volume = 0;
		if(volume > 4f) volume = 4f;
		
		volume = (float) (Math.round(volume * 100.0) / 100.0);
		
		TinySound.setGlobalVolume(1f + (volume - 0.5f) * 2f);
		super.setVolume(volume);
	}
	
	
}
