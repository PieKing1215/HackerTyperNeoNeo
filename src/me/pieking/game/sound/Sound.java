package me.pieking.game.sound;

import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Logger;

public class Sound {
	
	/*
	 * I've been having so many problems with this. I started using the JavaSound API with WAVs, but they're way too large.
	 * Problem is, the JavaSound API only supports WAV, AU, and AIFF (which aren't much better).
	 * So then I tried Paul Lamb's Sound Library, but it's too heavy duty and takes too long to play a sound (IDK, maybe I'm using it wrong?).
	 * Next was EasyOgg, which was good, but there's a memory leak and clicking sounds (the bane of my existence).
	 * So up next is the TinySound library, which also supports OGGs. I'm seeing good things, so fingers crossed! :P
	 * https://github.com/finnkuusisto/TinySound
	 * 
	 * OMG THANK YOU SO MUCH TINYSOUND WORKS SO MUCH BETTER!!!
	 * I redesigned the whole sound system to make it interchangable,
	 * but after implementing TinySound into it, I didn't bother re-implementing any others!
	 * I also really like how TinySound wraps the JavaSound API, and provides pretty much all the functionality I need right now!
	 * Just to put it into perspective, the backup I have of the sound folder is 919MB(uncompressed) with WAVs, and the new OGG one is 62MB(uncompressed).
	 */
	
//	public static String voiceGeneralA = "SFX/Voices/generic text noise.ogg";
//	public static String voiceGenericB = "SFX/Voices/generic text noise 2.ogg";
//	
//	public static String voiceToriel = "SFX/Voices/Toriel/Toriel voice.ogg";
//	public static String voiceTorielD1 = "SFX/Voices/Toriel/Toriel voice deeper.ogg";
//	public static String voiceTorielD2 = "SFX/Voices/Toriel/Toriel voice deeper 2.ogg";
//	
//	public static String voiceAsgore = "SFX/Voices/Asgore voice.ogg";
//	public static String voiceAsgoreS = "SFX/Voices/Asgore voice 2.ogg";
//	public static String voiceAsgoreW = "SFX/Voices/Asgore voice 3.ogg";
//	public static String voiceSans = "SFX/Voices/sans.ogg";
//	public static String voiceUndyne = "SFX/Voices/Undyne voice.ogg";
//	
//	public static String voiceFloweyNormal = "SFX/Voices/Flowey/Flowey voice.ogg";
//	public static String voiceFloweyDemonic = "SFX/Voices/Flowey/Flowey demon voice.ogg";
//	
//	public static String voiceAsriel = "SFX/Voices/Asriel voice.ogg";
//	
//	public static String voiceGaster = "gaster";
//	
//	public static String voiceMettaton = "mettaton";
	
	public static List<String> alreadyCredited = new ArrayList<String>();
	
/*
	public static Clip clip=null;
	public static List<ClipIds> clips = new ArrayList<ClipIds>();
	
	public static List<OggClipIds> oggClips = new ArrayList<OggClipIds>();
	public static List<OggClipIds> oggClipsDontStop = new ArrayList<OggClipIds>();
	
	public static List<Sound> sounds = new ArrayList<Sound>();
	
	public static SoundSystem soundSystem;
	
	/**
	 * From 0-1
	 ~/
	public static float masterVolume = 1f;
	
	public static List<String> currSounds = new ArrayList<String>();
	public static List<String> currSoundsDontStop = new ArrayList<String>();
	
	public static int id = 0;
	
	public static final boolean paulsCode = false;
	
	public static final boolean easyOgg = false;
	
	public static final boolean tinySound = true;
	
	public static List<AudioInputClip> loadedAudioStreams = new ArrayList<AudioInputClip>();
*/
	
	public static me.pieking.game.sound.SoundSystem soundSystem = new TinySoundSystem();
	
	//public static Minim minim;
	
	private static SoundClip test;
	
	public static void init(){
		Logger.info("Starting sound system...");
		
		soundSystem.init();
		soundSystem.setVolume(0.5f);
		
		//minim = new Minim(new MinimHandler());
		
		test = loadSound("volume.ogg");
		test.setVolume(0.1f);
	}
	
	public static void shutdown(int state){
		if(state != 666) Logger.info("Shutting down sound system...");
		
		soundSystem.shutdown();
	}
	
	public static SoundClip playSound(String name, float volume){
		return soundSystem.playSound(name, volume);
	}
	public static SoundClip playSound(String name, float volume, boolean loop){
		return soundSystem.playSoundSemiFinal(name, volume, loop);
	}
	
	public static SoundClip playSound(String name, float volume, boolean loop, boolean priority){
		return soundSystem.playSoundFinal(name, volume, loop, priority);
	}
	
	public static void stopSounds(){
		stopSounds(true);
	}
	
	public static void stopSounds(boolean closeSounds){
		Logger.info("stopSounds()", Logger.VB_DEV_ONLY);
		soundSystem.stopSounds(closeSounds);
	}
	
	public static void hardStopSounds(){
		hardStopSounds(true);
	}
	
	public static void hardStopSounds(boolean closeSounds){
		Logger.info("hardStopSounds()", Logger.VB_DEV_ONLY);
		soundSystem.hardStopSounds(closeSounds);
	}
	
	public static void stopSound(String name){
		soundSystem.stopSound(name);
	}
	
	public static void pause(){
		soundSystem.pause();
	}
	
	public static void unpause(){
		soundSystem.unpause();
	}
	
	public static void pause(String name){
		soundSystem.pause(name);
	}
	
	public static void unpause(String name){
		soundSystem.unpause(name);
	}
	
	public static SoundClip loadSound(String name){
		if(soundSystem.canLoadSounds()) return soundSystem.loadSound(name);
		return null;
	}
	
	public static RandomSoundClip loadRandomSound(String base, int num){
		List<SoundClip> clips = new ArrayList<SoundClip>();
		for(int i = 0; i < num; i++) clips.add(loadSound(base + i + ".ogg"));
		return new RandomSoundClip(clips);
	}
	
	public static void periodic(){
		soundSystem.periodic();
	}

	public static void tick() {
		soundSystem.tick();
	}
	
	public static void soundTest(){
		test.stop();
		test.start();
	}
	
}

