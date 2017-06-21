/*
 * Copyright (c) 2012, Finn Kuusisto
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package me.pieking.game.sound;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.SourceDataLine;

import kuusisto.tinysound.internal.Mixer;
import me.pieking.game.Game;

/**
 * The UpdateRunner2 class implements Runnable and is what performs automatic
 * updates of the TeenySound system.  UpdateRunner2 is an internal class of the
 * TeenySound system and should be of no real concern to the average user of
 * TeenySound.
 * 
 * @author Finn Kuusisto
 */
public class UpdateRunner2 implements Runnable {
		
		private AtomicBoolean running;
		private SourceDataLine outLine;
		private Mixer mixer;
		
		public float speed = 1f;
		
		/**
		 * Constructs a new UpdateRunner2 to update the TeenySound system.
		 * @param mixer the mixer to read audio data from
		 * @param outLine the line to write audio data to
		 */
		public UpdateRunner2(Mixer mixer, SourceDataLine outLine) {
			this.running = new AtomicBoolean();
			this.mixer = mixer;
			this.outLine = outLine;
		}
		
		/**
		 * Stop this UpdateRunner2 from updating the TeenySound system.
		 */
		public void stop() {
			this.running.set(false);
		}

		@Override
		public void run() {
			//mark the updater as running
			this.running.set(true);
			//1-sec buffer
			int bufSize = (int)TeenySound.FORMAT.getFrameRate() *
				TeenySound.FORMAT.getFrameSize();
			byte[] audioBuffer = new byte[bufSize];
			//only buffer some maximum number of frames each update (25ms)
			int maxFramesPerUpdate = 
				(int)((TeenySound.FORMAT.getFrameRate() / 1000) * 25);
			int numBytesRead = 0;
			double framesAccrued = 0;
			long lastUpdate = System.nanoTime();
			
			//keep running until told to stop
			while (this.running.get()) {
				//if(Game.time%5!=0) continue;
				//check the time
				long currTime = System.nanoTime();
				//accrue frames
				double delta = currTime - lastUpdate;
				double secDelta = (delta / 1000000000L);
				framesAccrued += secDelta * TeenySound.FORMAT.getFrameRate();
				//read frames if needed
				int framesToRead = (int)framesAccrued;
				int framesToSkip = 0;
				//check if we need to skip frames to catch up
				
				if (framesToRead > maxFramesPerUpdate) { //OMG COMMENTING THIS OUT MADE IT SOUND SO MUCH BETTER! (I won't need frame skip anyway...)
					framesToSkip = framesToRead - maxFramesPerUpdate;
					framesToRead = maxFramesPerUpdate;
				}
				
				//skip frames
				if (framesToSkip > 0) {
					int bytesToSkip = framesToSkip *
						TeenySound.FORMAT.getFrameSize();
					this.mixer.skip(bytesToSkip);
				}
				//read frames
				if (framesToRead > 0) {
					//read from the mixer
					int bytesToRead = framesToRead *
						TeenySound.FORMAT.getFrameSize();
					int tmpBytesRead = this.mixer.read(audioBuffer,
							numBytesRead, bytesToRead);
					numBytesRead += tmpBytesRead; //mark how many read
					//fill rest with zeroes
					int remaining = bytesToRead - tmpBytesRead;
					for (int i = 0; i < remaining; i++) {
						audioBuffer[numBytesRead + i] = 0;
					}
					numBytesRead += remaining; //mark zeroes read
				}
				//mark frames read and skipped
				framesAccrued -= (framesToRead + framesToSkip);
				//write to speakers
				if (numBytesRead > 0) {
					this.outLine.write(audioBuffer, 0, numBytesRead);
					numBytesRead = 0;
				}
				//mark last update
				lastUpdate = currTime;
				//give the CPU back to the OS for a bit
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {}
			}
		}
		
	}