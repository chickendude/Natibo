package ch.ralena.glossikaschedule.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.IOException;

import ch.ralena.glossikaschedule.MainActivity;
import ch.ralena.glossikaschedule.R;
import ch.ralena.glossikaschedule.object.Day;
import ch.ralena.glossikaschedule.object.Sentence;
import ch.ralena.glossikaschedule.object.SentencePair;
import ch.ralena.glossikaschedule.utils.Utils;
import io.reactivex.subjects.PublishSubject;
import io.realm.Realm;

public class StudySessionService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
	public static final String BROADCAST_START_SESSION = "broadcast_start_session";
	public static final String ACTION_PLAY = "action_play";
	public static final int ACTION_ID_PLAY = 0;
	public static final String ACTION_PAUSE = "action_pause";
	public static final int ACTION_ID_PAUSE = 1;
	public static final String ACTION_PREVIOUS = "action_previous";
	public static final int ACTION_ID_PREVIOUS = 2;
	public static final String ACTION_NEXT = "action_next";
	public static final int ACTION_ID_NEXT = 3;

	private static final int NOTIFICATION_ID = 1337;
	private static final String CHANNEL_ID = "GLOSSIKA_1337";

	// Media Session
	private MediaSessionManager mediaSessionManager;
	private MediaSessionCompat mediaSession;
	private MediaControllerCompat.TransportControls transportControls;

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
	private Sentence sentence;
	private PlaybackStatus playbackStatus;
	private NotificationCompat.Builder notificationBuilder;


	PublishSubject<SentencePair> sentencePublish = PublishSubject.create();
	PublishSubject<Day> finishPublish = PublishSubject.create();

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
		if (day == null) {
			day = realm.where(Day.class).equalTo("id", id).findFirst();
			if (day == null)
				stopSelf();
			day.resetReviews(realm);
		}


		if (!requestAudioFocus())
			stopSelf();

		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			loadSentence();
		}

		if (mediaSessionManager == null) {
			initMediaSession();
		}

		handleIncomingActions(intent);

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

		// stop media from playing
		if (mediaPlayer != null) {
			stop();
			mediaPlayer.release();
		}
		removeAudioFocus();

		// cancel the phone state listener
		if (phoneStateListener != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		removeNotification();

		// unregister broadcast receivers
		unregisterReceiver(becomingNoisyReceiver);
		unregisterReceiver(startSessionReceiver);
	}

	// --- setup ---

	private void loadSentence() {
		sentencePair = day.getCurrentSentencePair();

		// if sentencePair is null, we're done studying for the day!
		if (sentencePair == null) {
			removeNotification();
			finishPublish.onNext(day);
			stopSelf();
		} else {
			sentencePublish.onNext(sentencePair);
			sentence = day.getCurrentSentence();
			mediaPlayer.stop();
			mediaPlayer.reset();
			try {
				// load sentence path into mediaplayer to be played
				mediaPlayer.setDataSource(sentence.getUri());
				mediaPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
				stopSelf();
			}
			updateNotificationText();
		}
	}

	// --- managing media ---

	private void initMediaSession() {
		if (mediaSessionManager != null)
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
		}
		mediaSession = new MediaSessionCompat(getApplicationContext(), "GlossikaNative");
		transportControls = mediaSession.getController().getTransportControls();
		mediaSession.setActive(true);
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
		mediaSession.setCallback(new MediaSessionCompat.Callback() {
			@Override
			public void onPlay() {
				super.onPlay();
				resume();
				buildNotification();
			}

			@Override
			public void onPause() {
				super.onPause();
				pause();
				buildNotification();
			}

			@Override
			public void onSkipToNext() {
				super.onSkipToNext();
				nextSentence();
				buildNotification();
			}

			@Override
			public void onSkipToPrevious() {
				super.onSkipToPrevious();
				previousSentence();
				buildNotification();
			}

			@Override
			public void onSeekTo(long pos) {
				super.onSeekTo(pos);
			}
		});
	}

	private void updateNotificationText() {
		if (notificationBuilder != null) {
			notificationBuilder
					.setContentText(sentencePair.getBaseSentence().getText())
					.setContentTitle(sentencePair.getTargetSentence().getText())
					.setOngoing(playbackStatus == PlaybackStatus.PLAYING);
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

		}
	}

	private void play() {
		playbackStatus = PlaybackStatus.PLAYING;
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	private void stop() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
	}

	public void pause() {
		playbackStatus = PlaybackStatus.PAUSED;
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			stopPosition = mediaPlayer.getCurrentPosition();
			mediaPlayer.pause();
		}
	}

	public void resume() {
		playbackStatus = PlaybackStatus.PLAYING;
		if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	private void nextSentence() {
		day.goToNextSentencePair(realm);
		loadSentence();
		if (playbackStatus == PlaybackStatus.PLAYING) {
			play();
		}
	}

	private void previousSentence() {
		day.goToPreviousSentencePair(realm);
		loadSentence();
		if (playbackStatus == PlaybackStatus.PLAYING) {
			play();
		}
	}

	private void setVolume(float volume) {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.setVolume(volume, volume);
		}
	}

	// --- notification ---

	public void buildNotification() {
		int playPauseDrawable = android.R.drawable.ic_media_pause;
		PendingIntent playPauseAction = null;

		if (playbackStatus == PlaybackStatus.PLAYING) {
			playPauseDrawable = android.R.drawable.ic_media_pause;
			playPauseAction = iconAction(ACTION_ID_PAUSE);
		} else if (playbackStatus == PlaybackStatus.PAUSED) {
			playPauseDrawable = android.R.drawable.ic_media_play;
			playPauseAction = iconAction(ACTION_ID_PLAY);
		}

		// create the notification channel
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			CharSequence name = "GlossikaNativeChannel";
			String description = "Glossika stuff";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system
			notificationManager.createNotificationChannel(channel);
		}


		// create the notification
		Intent activityIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_LOAD_SESSION, activityIntent, 0);
		notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setContentIntent(contentIntent)
				.setShowWhen(false)
				.setOngoing(playbackStatus == PlaybackStatus.PLAYING)
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.ic_owl)
				.setStyle(
						new android.support.v4.media.app.NotificationCompat.MediaStyle()
								.setMediaSession(mediaSession.getSessionToken())
								.setShowActionsInCompactView(1)
				)
				.setContentText(sentencePair.getBaseSentence().getText())
				.setContentTitle(sentencePair.getTargetSentence().getText())
				.addAction(android.R.drawable.ic_media_previous, "prev sentence", iconAction(ACTION_ID_PREVIOUS))
				.addAction(playPauseDrawable, "pause", playPauseAction)
				.addAction(android.R.drawable.ic_media_next, "next sentence", iconAction(ACTION_ID_NEXT));
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	public void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		notificationManager.cancelAll();
	}

	private PendingIntent iconAction(int actionId) {
		Intent iconIntent = new Intent(this, StudySessionService.class);
		switch (actionId) {
			case ACTION_ID_PLAY:
				iconIntent.setAction(ACTION_PLAY);
				break;
			case ACTION_ID_PAUSE:
				iconIntent.setAction(ACTION_PAUSE);
				break;
			case ACTION_ID_NEXT:
				iconIntent.setAction(ACTION_NEXT);
				break;
			case ACTION_ID_PREVIOUS:
				iconIntent.setAction(ACTION_PREVIOUS);
				break;
			default:
				return null;
		}
		return PendingIntent.getService(this, actionId, iconIntent, 0);
	}

	private void handleIncomingActions(Intent playbackAction) {
		if (playbackAction == null || playbackAction.getAction() == null) return;

		String actionString = playbackAction.getAction();
		if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
			transportControls.play();
		} else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
			transportControls.pause();
		} else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
			transportControls.skipToNext();
		} else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
			transportControls.skipToPrevious();
		}
	}


	// --- call state listener

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
		if (day.nextSentence(realm)) {
			Handler handler = new Handler();
			Runnable runnable = () -> {
				loadSentence();
				if (playbackStatus == PlaybackStatus.PLAYING)
					play();
			};
			handler.postDelayed(runnable, 1000);
		}
//		stopSelf();
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
			loadSentence();
		}
		play();

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

	private void registerBecomingNoisyReceiver() {
		IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(becomingNoisyReceiver, intentFilter);
	}

	private void registerStartSessionReceiver() {
		IntentFilter intentFilter = new IntentFilter(BROADCAST_START_SESSION);
		registerReceiver(startSessionReceiver, intentFilter);
	}

	private class BecomingNoisyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				pause();
				buildNotification();
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

			loadSentence();
			play();
		}
	}

	public PublishSubject<SentencePair> sentenceObservable() {
		return sentencePublish;
	}

	public PublishSubject<Day> finishObservable() {
		return finishPublish;
	}

	// --- getters/setters ---

	public PlaybackStatus getPlaybackStatus() {
		return playbackStatus;
	}
}
