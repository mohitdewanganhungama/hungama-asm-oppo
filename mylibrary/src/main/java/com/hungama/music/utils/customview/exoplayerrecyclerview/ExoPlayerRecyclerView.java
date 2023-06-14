package com.hungama.music.utils.customview.exoplayerrecyclerview;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.hungama.music.utils.CommonUtils;
import com.hungama.music.R;
import com.hungama.music.data.model.MediaObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

@OptIn(markerClass = UnstableApi.class)
public class ExoPlayerRecyclerView extends RecyclerView {
    private static final String TAG = "ExoPlayerRecyclerView";
    private static final String AppName = "Android ExoPlayer";
    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private ImageView mediaCoverImage, volumeControl;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout mediaContainer;
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;
    /**
     * variable declaration
     */
    // Media List
    private ArrayList<MediaObject> mediaObjects = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;
    // controlling volume state
    private VolumeState volumeState;
    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleVolume();
        }
    };
    public ExoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }
    public ExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context.getApplicationContext();
        Display display = ((WindowManager) Objects.requireNonNull(
                getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;
        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        TrackSelector trackSelector =
                new DefaultTrackSelector();
        //Create the player using ExoPlayerFactory
        videoPlayer= new SimpleExoPlayer.Builder(context, new DefaultRenderersFactory(context)).build();

        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        setVolumeControl(VolumeState.OFF);
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mediaCoverImage != null) {
                        // show the old thumbnail
                        mediaCoverImage.setVisibility(VISIBLE);
                    }
                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true, true);
                    } else {
                        playVideo(false, true);
                    }
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
            }
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });
        videoPlayer.addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {

            }

            @Override
            public void onTracksChanged(Tracks tracks) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        CommonUtils.INSTANCE.setLog(TAG, "onPlayerStateChanged: Buffering video.");
                        if (progressBar != null) {
                            progressBar.setVisibility(VISIBLE);
                        }
                        break;
                    case Player.STATE_ENDED:
                        CommonUtils.INSTANCE.setLog(TAG, "onPlayerStateChanged: Video ended.");
                        videoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        CommonUtils.INSTANCE.setLog(TAG, "onPlayerStateChanged: Ready to play.");
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        if (!isVideoViewAdded) {
                            addVideoView();
                        }
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onRepeatModeChanged(int repeatMode) {
            }
            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            }
            @Override
            public void onPlayerError(@NotNull PlaybackException error) {
            }
            @Override
            public void onPositionDiscontinuity(int reason) {
            }
            @Override
            public void onPlaybackParametersChanged(@NotNull PlaybackParameters playbackParameters) {
            }
            @Override
            public void onSeekProcessed() {
            }
        });
    }
    public void playVideo(boolean isEndOfList, boolean isPlayWhenReady) {
        int targetPosition;
        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager) Objects.requireNonNull(
                    getLayoutManager())).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }
            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }
            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
                targetPosition =
                        startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = mediaObjects.size() - 1;
        }
        CommonUtils.INSTANCE.setLog(TAG, "playVideo: target position: " + targetPosition);
        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }
        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        if (videoSurfaceView == null) {
            return;
        }
        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);
        int currentPosition =
                targetPosition - ((LinearLayoutManager) Objects.requireNonNull(
                        getLayoutManager())).findFirstVisibleItemPosition();
        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }
        PlayerViewHolder holder = (PlayerViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }
        mediaCoverImage = holder.mediaCoverImage;
        progressBar = holder.progressBar;
        volumeControl = holder.volumeControl;
        viewHolderParent = holder.itemView;
        requestManager = holder.requestManager;
        mediaContainer = holder.mediaContainer;
        videoSurfaceView.setPlayer(videoPlayer);
        viewHolderParent.setOnClickListener(videoViewClickListener);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context, Util.getUserAgent(context, AppName));
        String mediaUrl = mediaObjects.get(targetPosition).getUrl();
        if (mediaUrl != null) {
            MediaSource videoSource=new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory())
                    .createMediaSource(MediaItem.fromUri(Uri.parse(mediaUrl)));
            videoPlayer.prepare(videoSource);
            videoPlayer.setPlayWhenReady(isPlayWhenReady);
        }
    }
    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) Objects.requireNonNull(
                getLayoutManager())).findFirstVisibleItemPosition();
        CommonUtils.INSTANCE.setLog(TAG, "getVisibleVideoSurfaceHeight: at: " + at);
        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }
        int[] location = new int[2];
        child.getLocationInWindow(location);
        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }
    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }
        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }
    }
    private void addVideoView() {
        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
        mediaCoverImage.setVisibility(GONE);
    }
    private void resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            videoSurfaceView.setVisibility(INVISIBLE);
            mediaCoverImage.setVisibility(VISIBLE);
        }
    }
    public void releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
        viewHolderParent = null;
    }
    public void onStopPlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop(true);
        }
    }
    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }
    public void onPlayPlayer() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
    }
    private void toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                CommonUtils.INSTANCE.setLog(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);
            } else if (volumeState == VolumeState.ON) {
                CommonUtils.INSTANCE.setLog(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);
            }
        }
    }
    private void setVolumeControl(VolumeState state) {
        volumeState = state;
        if (state == VolumeState.OFF) {
            videoPlayer.setVolume(0f);
            animateVolumeControl();
        } else if (state == VolumeState.ON) {
            videoPlayer.setVolume(1f);
            animateVolumeControl();
        }
    }
    private void animateVolumeControl() {
        if (volumeControl != null) {
            volumeControl.bringToFront();
            if (volumeState == VolumeState.OFF) {
                requestManager.load(R.drawable.volume_mute)
                        .into(volumeControl);
            } else if (volumeState == VolumeState.ON) {
                requestManager.load(R.drawable.volume)
                        .into(volumeControl);
            }
            volumeControl.animate().cancel();
            volumeControl.setAlpha(1f);
            volumeControl.animate()
                    .alpha(0f)
                    .setDuration(600).setStartDelay(1000);
        }
    }
    public void setMediaObjects(ArrayList<MediaObject> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }
    /**
     * Volume ENUM
     */
    private enum VolumeState {
        ON, OFF
    }
}
