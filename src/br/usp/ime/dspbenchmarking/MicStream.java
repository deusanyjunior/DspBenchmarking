package br.usp.ime.dspbenchmarking;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.SystemClock;

public class MicStream extends AudioStream {

	private int bufferSize = 0;
	private int sampleRate;
	private int ix = 0;

	AudioRecord recorder = null;
	
	public MicStream(int bufSize, int sRate, int blSize) {
		bufferSize = bufSize;
		sampleRate = sRate;
		blockSize = blSize;
		// initiate the recording from microphone.
		recorder = new AudioRecord(AudioSource.MIC, // Audio source
				// (could be
				// VOICE_UPLINK).
				sampleRate, // Sample rate (Hz) -- 44.100 is the only
				// guaranteed
				// to work on all devices.
				AudioFormat.CHANNEL_IN_MONO, // Channel configuration
				// (could be
				// CHANNEL_IN_STEREO,
				// not guaranteed to
				// work).
				AudioFormat.ENCODING_PCM_16BIT, // Channel encoding
				// (could be
				// ENCODING_PCM_8BIT,
				// not guaranteed to
				// work).
				getMinBufferSize() * 10); // buffer size.

	}
	
	public short[] createBuffer() {
		return new short[bufferSize];
	}

	public void scheduleDspCallback(long blockPeriodNanoseconds) {
		recorder.setPositionNotificationPeriod(blockSize);
		recorder.setRecordPositionUpdateListener(microphoneDspCallback);
		recorder.startRecording();
	}

	public int blocks() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void stopRunning() {
		isRunning = false;
	}
	
	/**
	 * Listener for when using AUDIO_SOURCE_MICROPHONE.
	 */
	AudioRecord.OnRecordPositionUpdateListener microphoneDspCallback = new AudioRecord.OnRecordPositionUpdateListener() {
		private long lastListenerStartTime = 0;

		public void onPeriodicNotification(AudioRecord recorder) {
			// Takes note of time between listeners
			long startTime = SystemClock.uptimeMillis();
			if (lastListenerStartTime != 0)
				callbackPeriod += (startTime - lastListenerStartTime);
			lastListenerStartTime = startTime;
			dspCallback.run();
			// if (callbackTicks == 1000)
			// stopRunning();
		}

		public void onMarkerReached(AudioRecord recorder) {
		}
	};
	
	/**
	 * Returns the minumum buffer size for a given DSP configuration.
	 * 
	 * @return
	 */
	protected int getMinBufferSize() {
		return AudioRecord.getMinBufferSize(sampleRate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
	}
	

	public void readLoop(short[] buffer) {
		long times1, times2;
		while (isRunning) {
			readTicks++;
			// read to buffer
			times1 = SystemClock.uptimeMillis();
			if (recorder != null)
				recorder.read(buffer, (ix++ % bufferSize)
						* blockSize, blockSize);
			times2 = SystemClock.uptimeMillis();
			sampleReadTime += (times2 - times1);
			// calculate elapsed time
			// if (elapsedTime > (100000.0 * (float) blockSize /
			// sampleRate))
			// stopRunning();
		}
	}

}
