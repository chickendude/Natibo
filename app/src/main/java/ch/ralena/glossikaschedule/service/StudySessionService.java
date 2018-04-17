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

import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.object.SentencePair;
import ch.ralena.glossikaschedule.utils.Utils;
import io.realm.Realm;

public class StudySessionService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
	public static final String KEY_SENTENCE_PATH = "tag_sentence_path";
	public static final String KEY_DAY_ID = "key_day_id";
	public static final String BROADCAST_START_SESSION = "broadcast_start_session";

	public enum PlaybackStatus {
		PLAYING, PAUSED
	}

	private MediaPlayer mediaPlayer;
	private Day day;
	private int stopPosition;
	private AudioManager audioManager;
	private boolean inCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;
	private Realm realm;
	private SentencePair sentencePair;

	// given to clients that connect to the service
	StudyBinder binder = new StudyBinder();

	// Broadcast Receivers
	private BroadcastReceiver becomingNoisyReceiver = new BecomingNoisyReceiver();
	private BroadcastReceiver startSessionReceiver = new StartSessionReceiver();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// check if we have attached our bundle or not
		if (intent.getExtras() == null)
			stopSelf();

		realm = Realm.getDefaultInstance();

		String id = new Utils.Storage(getApplicationContext()).getDayId();
		day = realm.where(Day.class).equalTo("id", id).findFirst();
		if (day == null)
			stopSelf();
		day.resetReviews(realm);

		if (!requestAudioFocus())
			stopSelf();

		playSentence();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		callStateListener();
		registerBecomingNoisyReceiver();
		registerStartSessionReceiver();
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

	// --- setup ---
	private void playSentence() {
		sentencePair = day.getNextSentencePair(realm);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.reset();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			// load sentence path into mediaplayer to be played
			mediaPlayer.setDataSource(sentencePair.getTargetSentence().getUri());
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

	private void registerStartSessionReceiver() {
		IntentFilter intentFilter = new IntentFilter(BROADCAST_START_SESSION);
		registerReceiver(startSessionReceiver, intentFilter);
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
			playSentence();
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

	// --- Broadcast Receivers ---

	private class BecomingNoisyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				pause();
//				buildNotification(PlaybackStatus.PAUSED);
			}
		}
	}

	private class StartSessionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String id = new Utils.Storage(getApplicationContext()).getDayId();
			day = realm.where(Day.class).equalTo("id", id).findFirst();
			if (day == null)
				stopSelf();
			day.resetReviews(realm);
			stop();
			mediaPlayer.reset();
			if (!requestAudioFocus())
				stopSelf();

			playSentence();
		}
	}
}
