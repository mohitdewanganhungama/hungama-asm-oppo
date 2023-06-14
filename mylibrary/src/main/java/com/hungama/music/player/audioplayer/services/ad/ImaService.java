package com.hungama.music.player.audioplayer.services.ad;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.gson.Gson;
import com.hungama.music.player.audioplayer.model.Track;
import com.hungama.music.player.audioplayer.player.ExoPlayer;
import com.hungama.music.player.audioplayer.services.AudioPlayerService;
import com.hungama.music.ui.base.BaseActivity;
import com.hungama.music.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes control of audio playback from the AudioPlayerService to play some ads and then returns
 * control afterwards.
 */
public class ImaService
    implements AdErrorEvent.AdErrorListener, AdEvent.AdEventListener, AdsLoader.AdsLoadedListener {

  private static final String LOGGING_TAG = "ImaService";

  private AdsLoader adsLoader;
  private AdsManager adsManager;
  private AdMediaInfo currentAd;
  private ImaProgressTracker progressTracker;
  private final Context context;
  private final AudioPlayerService.SharedAudioPlayer sharedAudioPlayer;
  private final ExoPlayer exoPlayer;
  private final List<VideoAdPlayerCallback> callbacks;
  private final ImaSdkFactory sdkFactory;
  private final ImaSdkSettings imaSdkSettings;
  private final DefaultDataSourceFactory dataSourceFactory;
  private BaseActivity.OnImaAdsListener mImaListener = null;

  public ImaVideoAdPlayer imaVideoAdPlayer = new ImaVideoAdPlayer();
  @OptIn(markerClass = UnstableApi.class)
  public ImaService(
      Context context,
      DefaultDataSourceFactory dataSourceFactory,
      AudioPlayerService.SharedAudioPlayer sharedAudioPlayer) {
    this.context = context;
    this.sharedAudioPlayer = sharedAudioPlayer;
    this.exoPlayer = sharedAudioPlayer.getPlayer();
    this.callbacks = new ArrayList<>();
    this.sdkFactory = ImaSdkFactory.getInstance();
    this.imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
    this.dataSourceFactory = dataSourceFactory;
    sharedAudioPlayer.addAnalyticsListener(new ImaListener());
  }

  /**
   * Initializes the ImaService. Note: Ad playback with CompanionAds requires an AdDisplayContainer
   * from the MainActivity.
   */
  public void init(AdDisplayContainer adDisplayContainer) {
    adsLoader = sdkFactory.createAdsLoader(context, imaSdkSettings, adDisplayContainer);
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);

    progressTracker = new ImaProgressTracker(imaVideoAdPlayer);
  }

  public void requestAds(String adTagUrl) {
    try {
      Log.e("AudioSongData", " ImageService " + new Gson().toJson(adTagUrl));

      AdsRequest request = sdkFactory.createAdsRequest();
      request.setAdTagUrl(adTagUrl);
      Log.e("AudioSongData", " ImageService " + new Gson().toJson(adTagUrl));

      // The ContentProgressProvider is only needed for scheduling ads with VMAP ad requests
      request.setContentProgressProvider(
              new ContentProgressProvider() {
                @OptIn(markerClass = UnstableApi.class)
                @Override
                public VideoProgressUpdate getContentProgress() {
                  //CommonUtils.INSTANCE.setLog("ImaAdsService", "ImaService-currentPosition-"+exoPlayer.getSimpleExoPlayer().getCurrentPosition() + "-Duration-" + exoPlayer.getSimpleExoPlayer().getDuration());
                  return new VideoProgressUpdate(exoPlayer.getSimpleExoPlayer().getCurrentPosition(), exoPlayer.getSimpleExoPlayer().getDuration());
                }
              });
      if (adsLoader != null){
        adsLoader.requestAds(request);
      }
    }catch (Exception e){
Log.e("eahoahoga", e.getMessage());
    }
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
    adsManager = adsManagerLoadedEvent.getAdsManager();
    adsManager.addAdErrorListener(this);
    adsManager.addAdEventListener(this);
    adsManager.init();
  }

  @Override
  public void onAdError(AdErrorEvent adErrorEvent) {
    CommonUtils.INSTANCE.setLog(LOGGING_TAG, "Ad Error: code :"+adErrorEvent.getError().getErrorCode()+"\nadErrorEvent:" + adErrorEvent.getError().getMessage());
    if (mImaListener != null){
      mImaListener.onImaAdError(adErrorEvent);
    }else {
      CommonUtils.INSTANCE.setLog(LOGGING_TAG, "Ad Error: mImaListener - null");
    }
  }

  @Override
  public void onAdEvent(AdEvent adEvent) {
    CommonUtils.INSTANCE.setLog(LOGGING_TAG, "Event: " + adEvent.getType());
    if (mImaListener != null){
      mImaListener.onImaAdEvent(adEvent);
    }
    switch (adEvent.getType()) {
      case LOADED:
        if (adsManager != null) {
          // If preloading we may to call start() at a particular time offset, instead of immediately.
          adsManager.start();
        }
        break;
      case CONTENT_PAUSE_REQUESTED:
        sharedAudioPlayer.claim();
        break;
      case CONTENT_RESUME_REQUESTED:
        sharedAudioPlayer.release();
        break;
      case ALL_ADS_COMPLETED:
        if (adsManager != null) {
          adsManager.destroy();
          adsManager = null;
        }
        break;
      default:
        break;
    }
  }

  /** Allows IMA to tell the custom player what to do. */
  class ImaVideoAdPlayer implements VideoAdPlayer {
    @Override
    public void loadAd(AdMediaInfo adMediaInfo, AdPodInfo adPodInfo) {
      // If implementing preloading, we would pass the url back to the audio player here.
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void playAd(AdMediaInfo adMediaInfo) {
      String url = adMediaInfo.getUrl();
      CommonUtils.INSTANCE.setLog(LOGGING_TAG, "MediaUrlAds " + url);

      progressTracker.start();
      if (currentAd == adMediaInfo) {
        for (VideoAdPlayerCallback callback : callbacks) {
          callback.onResume(adMediaInfo);
        }
      } else {
        currentAd = adMediaInfo;
        for (VideoAdPlayerCallback callback : callbacks) {
          callback.onPlay(adMediaInfo);
        }
        Track track = new Track();
        track.setUrl(url);
        MediaSource mediaSource =
            new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(CommonUtils.INSTANCE.setMediaItem(track));
        sharedAudioPlayer.prepare(mediaSource);
      }
      exoPlayer.getSimpleExoPlayer().setPlayWhenReady(true);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void pauseAd(AdMediaInfo adMediaInfo) {
      exoPlayer.getSimpleExoPlayer().setPlayWhenReady(false);
      progressTracker.stop();
      for (VideoAdPlayerCallback callback : callbacks) {
        callback.onPause(adMediaInfo);
      }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void stopAd(AdMediaInfo adMediaInfo) {
      progressTracker.stop();
      exoPlayer.getSimpleExoPlayer().setPlayWhenReady(false);
      notifyEnded();
    }

    @Override
    public void release() {
      // Add any additional clean-up of resource used by the VideoAdPlayer.
    }

    @Override
    public void addCallback(VideoAdPlayerCallback callback) {
      callbacks.add(callback);
    }

    @Override
    public void removeCallback(VideoAdPlayerCallback callback) {
      callbacks.remove(callback);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public VideoProgressUpdate getAdProgress() {
      if (currentAd == null) {
        return null;
      }

      return new VideoProgressUpdate(exoPlayer.getSimpleExoPlayer().getCurrentPosition(), exoPlayer.getSimpleExoPlayer().getDuration());
    }

    void sendProgressUpdate() {
      for (VideoAdPlayerCallback callback : callbacks) {
        callback.onAdProgress(currentAd, getAdProgress());
      }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public int getVolume() {
      if (exoPlayer.getSimpleExoPlayer().getAudioComponent() == null) {
        return -1;
      } else {
        return (int) (100 * exoPlayer.getSimpleExoPlayer().getAudioComponent().getVolume());
      }
    }
  }

  @UnstableApi
          /** Encapsulates callbacks for ExoPlayer changes, and lets IMA know the state of playback */
  class ImaListener implements AnalyticsListener {
    @Override
    public void onPlaybackStateChanged(EventTime eventTime, int playbackState) {
      if (currentAd == null) {
        // This may be null if state changes after stopAd for a given mediaInfo
        return;
      }
      switch (playbackState) {
        case Player.STATE_BUFFERING:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onBuffering(currentAd);
          }
          break;
        case Player.STATE_READY:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onLoaded(currentAd);
          }
          break;
        case Player.STATE_ENDED:
          // Handles when the media item in the source is completed.
          notifyEnded();
          break;
        default:
          break;
      }
    }

    @Override
    public void onPlayWhenReadyChanged(
        EventTime eventTime, boolean playWhenReady, int playbackState) {
      if (currentAd == null) {
        // This may be null if state changes after stopAd for a given mediaInfo
        return;
      }
      switch (playbackState) {
        case Player.STATE_BUFFERING:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onBuffering(currentAd);
          }
          break;
        case Player.STATE_READY:
          for (VideoAdPlayerCallback callback : callbacks) {
            callback.onLoaded(currentAd);
          }
          break;
        case Player.STATE_ENDED:
          // Handles when the media item in the source is completed.
          notifyEnded();
          break;
        default:
          break;
      }
    }
  }

  private void notifyEnded() {
    for (VideoAdPlayerCallback callback : callbacks) {
      callback.onEnded(currentAd);
    }
  }

  static class ImaProgressTracker implements Handler.Callback {
    static final int START = 0;
    static final int UPDATE = 1;
    static final int QUIT = 2;
    static final int UPDATE_PERIOD_MS = 1000;
    private final Handler messageHandler;
    private final ImaVideoAdPlayer player;

    ImaProgressTracker(ImaVideoAdPlayer player) {
      this.messageHandler = new Handler(this);
      this.player = player;
    }

    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case QUIT:
          // Don't remove START message since it is yet to send updates. The QUIT message comes from
          // the current ad being updated, hence we remove UPDATE messages.
          messageHandler.removeMessages(UPDATE);
          break;
        case UPDATE:
          // Intentional fallthrough. START message is introduced as a way to differentiate the
          // beginning (the START of progress event) and progress itself (UPDATE events). Handling
          // for both the messages are same.
        case START:
          player.sendProgressUpdate();
          messageHandler.removeMessages(UPDATE);
          messageHandler.sendEmptyMessageDelayed(UPDATE, UPDATE_PERIOD_MS);
          break;
        default:
          break;
      }
      return true;
    }

    void start() {
      messageHandler.sendEmptyMessage(START);
    }

    void stop() {
      messageHandler.sendMessageAtFrontOfQueue(Message.obtain(messageHandler, QUIT));
    }
  }

  public void setImaAdsListener(BaseActivity.OnImaAdsListener imaAudioAdsListener){
    mImaListener = imaAudioAdsListener;
  }
}
