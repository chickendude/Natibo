package ch.ralena.glossikaschedule.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.IOException;

public class StudySessionService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
	public static final String KEY_SENTENCE_PATH = "tag_sentence_path";

	private MediaPlayer mediaPlayer;
	private String sentencePath;
	private int stopPosition;
	private AudioManager audioManager;
	private boolean inCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;

	// given to clients that connect to the service
	StudyBinder binder = new StudyBinder();

	// Broadcast Receivers
	private BroadcastReceiver becomingNoisyReceiver = new BecomingNoisyReceiver();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// check if we have attached our bundle or not
		if (intent.getExtras() == null)
			stopSelf();

		// load the sentence path
		sentencePath = intent.getExtras().getString(KEY_SENTENCE_PATH);
		if (sentencePath == null || sentencePath.equals(""))
			stopSelf();

		if (!requestAudioFocus())
			stopSelf();

		initialize();

		return super.onStartCommand(intent, flags, startId);
	}

	// --- setup ---
	private void initialize() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.reset();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			// load sentence path into mediaplayer to be played
			mediaPlayer.setDataSource(sentencePath);
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
			stopSelf();
		}
		play();
	}

	// --- managing media ---

	private void play() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	private void stop() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
	}

	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			stopPosition = mediaPlayer.getCurrentPosition();
		}
	}

	private void resume() {
		if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(stopPosition);
			mediaPlayer.start();
		}
	}

	private void setVolume(float volume) {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.setVolume(volume, volume);
		}
	}


	private void registerBecomingNoisyReceiver() {
		IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(becomingNoisyReceiver, intentFilter);
	}

	private void callStateListener() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						// phone ringing or in phone call
						inCall = true;
						pause();
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						// back from phone call
						inCall = false;
						resume();
						break;
				}
			}
		};
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// when file has completed playing
		stop();
		stopSelf();
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		// when another app makes focus request
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:      // we've (re)gained audio focus
				restartPlaying();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:        // we've lost focus indefinitely
				pauseAndRelease();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:    // we've lost focus for a short amount of time, e.g. Google Maps announcing directions
				stop();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:    // we've lost focus for a short amount of time but we can still play audio in bg
				setVolume(0.1f);
				break;

		}
	}

	private boolean requestAudioFocus() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

	private boolean removeAudioFocus() {
		return audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

	private void pauseAndRelease() {
		stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	private void restartPlaying() {
		if (mediaPlayer == null) {
			initialize();
		} else {
			play();
		}
		// restore full volume levels
		setVolume(1.0f);
	}

	// get a copy of the service so we can run its methods from fragment
	public class StudyBinder extends Binder {
		public StudySessionService getService() {
			return StudySessionService.this;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			stop();
			mediaPlayer.release();
		}
		removeAudioFocus();
	}

	private class BecomingNoisyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				pause();
			}
		}
	}

}
