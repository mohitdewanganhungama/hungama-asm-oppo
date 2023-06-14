/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hungama.music.player.download;

import static androidx.media3.common.util.Assertions.checkNotNull;
import static androidx.media3.common.util.Assertions.checkState;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

import com.google.common.collect.ImmutableList;
import com.hungama.music.player.audioplayer.model.Track;
import com.hungama.music.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util to read from and populate an intent.
 */
@OptIn(markerClass = UnstableApi.class) public class IntentUtil {

  // Actions.

  public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
  public static final String ACTION_VIEW_LIST =
          "com.google.android.exoplayer.demo.action.VIEW_LIST";

  // Activity extras.

  public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";

  // Media item configuration extras.

  public static final String URI_EXTRA = "uri";
  public static final String MIME_TYPE_EXTRA = "mime_type";
  public static final String CLIP_START_POSITION_MS_EXTRA = "clip_start_position_ms";
  public static final String CLIP_END_POSITION_MS_EXTRA = "clip_end_position_ms";

  public static final String AD_TAG_URI_EXTRA = "ad_tag_uri";

  public static final String DRM_SCHEME_EXTRA = "drm_scheme";
  public static final String DRM_LICENSE_URI_EXTRA = "drm_license_uri";
  public static final String DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties";
  public static final String DRM_SESSION_FOR_CLEAR_CONTENT = "drm_session_for_clear_content";
  public static final String DRM_MULTI_SESSION_EXTRA = "drm_multi_session";
  public static final String DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA = "drm_force_default_license_uri";

  public static final String SUBTITLE_URI_EXTRA = "subtitle_uri";
  public static final String SUBTITLE_MIME_TYPE_EXTRA = "subtitle_mime_type";
  public static final String SUBTITLE_LANGUAGE_EXTRA = "subtitle_language";

  /**
   * Creates a list of {@link MediaItem media items} from an {@link Intent}.
   */
  public static List<MediaItem> createMediaItemsFromIntent(Intent intent, List<Track> trackList) {
    List<MediaItem> mediaItems = new ArrayList<>();
    if (ACTION_VIEW_LIST.equals(intent.getAction())) {
      int index = 0;
      while (intent.hasExtra(URI_EXTRA + "_" + index)) {
        Uri uri = Uri.parse(intent.getStringExtra(URI_EXTRA + "_" + index));
        Track track = null;
        if (trackList != null){
          if (trackList.size() == 1){
            track = trackList.get(0);
          }else if (trackList.size() > index){
            track = trackList.get(index);
          }
        }

        CommonUtils.INSTANCE.setLog("DWContentTitle", "contentTitle-1-createMediaItemsFromIntent-URI-"+uri.toString());
        /*if (track != null && !track.getUrl().equals(uri.toString())){
          CommonUtils.INSTANCE.setLog("DWContentTitle", "contentTitle-1-createMediaItemsFromIntent-trackUrl-"+track.getUrl());
          track = null;
        }*/
        CommonUtils.INSTANCE.setLog("DWContentTitle", "contentTitle-1-createMediaItemsFromIntent-track-"+track);
        mediaItems.add(createMediaItemFromIntent(uri, intent, /* extrasKeySuffix= */ "_" + index, track));
        index++;
      }
    } else {
      Uri uri = intent.getData();
      Track track = null;
      if (trackList != null && trackList.size() > 0){
        track = trackList.get(0);
      }
      CommonUtils.INSTANCE.setLog("DWContentTitle", "contentTitle-2-createMediaItemsFromIntent-track"+track);
      mediaItems.add(createMediaItemFromIntent(uri, intent, /* extrasKeySuffix= */ "", track));
    }
    return mediaItems;
  }

  /**
   * Populates the intent with the given list of {@link MediaItem media items}.
   */
  public static void addToIntent(List<MediaItem> mediaItems, Intent intent) {
    Assertions.checkArgument(!mediaItems.isEmpty());
    if (mediaItems.size() == 1) {
      MediaItem mediaItem = mediaItems.get(0);
      MediaItem.PlaybackProperties playbackProperties = checkNotNull(mediaItem.playbackProperties);
      intent.setAction(ACTION_VIEW).setData(mediaItem.playbackProperties.uri);
      addPlaybackPropertiesToIntent(playbackProperties, intent, /* extrasKeySuffix= */ "");
      addClippingPropertiesToIntent(
              mediaItem.clippingProperties, intent, /* extrasKeySuffix= */ "");
    } else {
      intent.setAction(ACTION_VIEW_LIST);
      for (int i = 0; i < mediaItems.size(); i++) {
        MediaItem mediaItem = mediaItems.get(i);
        MediaItem.PlaybackProperties playbackProperties =
                checkNotNull(mediaItem.playbackProperties);
        intent.putExtra(URI_EXTRA + ("_" + i), playbackProperties.uri.toString());
        addPlaybackPropertiesToIntent(playbackProperties, intent, /* extrasKeySuffix= */ "_" + i);
        addClippingPropertiesToIntent(
                mediaItem.clippingProperties, intent, /* extrasKeySuffix= */ "_" + i);
      }
    }
  }

  private static MediaItem createMediaItemFromIntent(
          Uri uri, Intent intent, String extrasKeySuffix, Track track) {
    @Nullable String mimeType = intent.getStringExtra(MIME_TYPE_EXTRA + extrasKeySuffix);
    String title = ""+uri;
    if (track != null){
      if (!TextUtils.isEmpty(track.getTitle())){
        title = track.getTitle();
      }
    }
    CommonUtils.INSTANCE.setLog("DWContentTitle", "contentTitle-createMediaItemFromIntent-"+title);
    MediaItem.Builder builder =
            new MediaItem.Builder()
                    .setUri(uri)
                    .setMimeType(mimeType)
                    .setMediaMetadata(new MediaMetadata.Builder().setTitle("" + title).build())
                    .setAdTagUri(intent.getStringExtra(AD_TAG_URI_EXTRA + extrasKeySuffix))
                    .setSubtitles(createSubtitlesFromIntent(intent, extrasKeySuffix))

                    .setClipStartPositionMs(
                            intent.getLongExtra(CLIP_START_POSITION_MS_EXTRA + extrasKeySuffix, 0))
                    .setClipEndPositionMs(
                            intent.getLongExtra(
                                    CLIP_END_POSITION_MS_EXTRA + extrasKeySuffix, C.TIME_END_OF_SOURCE));

    return populateDrmPropertiesFromIntent(builder, intent, extrasKeySuffix).build();
  }

  private static List<MediaItem.Subtitle> createSubtitlesFromIntent(
          Intent intent, String extrasKeySuffix) {
    if (!intent.hasExtra(SUBTITLE_URI_EXTRA + extrasKeySuffix)) {
      return Collections.emptyList();
    }
    return Collections.singletonList(
            new MediaItem.Subtitle(
                    Uri.parse(intent.getStringExtra(SUBTITLE_URI_EXTRA + extrasKeySuffix)),
                    checkNotNull(intent.getStringExtra(SUBTITLE_MIME_TYPE_EXTRA + extrasKeySuffix)),
                    intent.getStringExtra(SUBTITLE_LANGUAGE_EXTRA + extrasKeySuffix),
                    C.SELECTION_FLAG_DEFAULT));
  }

  private static MediaItem.Builder populateDrmPropertiesFromIntent(
          MediaItem.Builder builder, Intent intent, String extrasKeySuffix) {
    String schemeKey = DRM_SCHEME_EXTRA + extrasKeySuffix;
    @Nullable String drmSchemeExtra = intent.getStringExtra(schemeKey);
    if (drmSchemeExtra == null) {
      return builder;
    }
    Map<String, String> headers = new HashMap<>();
    @Nullable
    String[] keyRequestPropertiesArray =
            intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA + extrasKeySuffix);
    if (keyRequestPropertiesArray != null) {
      for (int i = 0; i < keyRequestPropertiesArray.length; i += 2) {
        headers.put(keyRequestPropertiesArray[i], keyRequestPropertiesArray[i + 1]);
      }
    }
//    builder.setDrmConfiguration(
//            new MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                    .setScheme(Util.getDrmUuid(Util.castNonNull(drmSchemeExtra)))
//                    .setLicenseUri(intent.getStringExtra(DRM_LICENSE_URI_EXTRA + extrasKeySuffix))
//                    .setMultiSession(intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA + extrasKeySuffix, false))
//                    .setForceDefaultLicenseUri(intent.getBooleanExtra(DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA + extrasKeySuffix, false))
//                    .setLicenseRequestHeaders(headers)
//                    .setForcedSessionTrackTypes(ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO))
//                    .build());
    builder
            .setDrmUuid(Util.getDrmUuid(Util.castNonNull(drmSchemeExtra)))
            .setDrmLicenseUri(intent.getStringExtra(DRM_LICENSE_URI_EXTRA + extrasKeySuffix))
            .setDrmMultiSession(
                    intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA + extrasKeySuffix, false))
            .setDrmForceDefaultLicenseUri(
                    intent.getBooleanExtra(DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA + extrasKeySuffix, false))
            .setDrmLicenseRequestHeaders(headers);
    if (intent.getBooleanExtra(DRM_SESSION_FOR_CLEAR_CONTENT + extrasKeySuffix, false)) {
      builder.setDrmSessionForClearTypes(ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO));
    }
    return builder;
  }

  private static void addPlaybackPropertiesToIntent(
          MediaItem.PlaybackProperties playbackProperties, Intent intent, String extrasKeySuffix) {
    intent
            .putExtra(MIME_TYPE_EXTRA + extrasKeySuffix, playbackProperties.mimeType)
            .putExtra(
                    AD_TAG_URI_EXTRA + extrasKeySuffix,
                    playbackProperties.adsConfiguration != null
                            ? playbackProperties.adsConfiguration.adTagUri.toString()
                            : null);
    if (playbackProperties.drmConfiguration != null) {
      addDrmConfigurationToIntent(playbackProperties.drmConfiguration, intent, extrasKeySuffix);
    }
    if (playbackProperties!=null&&playbackProperties.subtitleConfigurations!=null&&playbackProperties.subtitleConfigurations.size()>0) {

//      checkState(playbackProperties.subtitles.size() == 1);
//      MediaItem.Subtitle subtitle = playbackProperties.subtitles.get(0);
      MediaItem.SubtitleConfiguration subtitle = playbackProperties.subtitleConfigurations.get(0);
      intent.putExtra(SUBTITLE_URI_EXTRA + extrasKeySuffix, subtitle.uri.toString());
      intent.putExtra(SUBTITLE_MIME_TYPE_EXTRA + extrasKeySuffix, subtitle.mimeType);
      intent.putExtra(SUBTITLE_LANGUAGE_EXTRA + extrasKeySuffix, subtitle.language);
    }
  }

  private static void addDrmConfigurationToIntent(
          MediaItem.DrmConfiguration drmConfiguration, Intent intent, String extrasKeySuffix) {
    intent.putExtra(DRM_SCHEME_EXTRA + extrasKeySuffix, drmConfiguration.uuid.toString());
    intent.putExtra(
            DRM_LICENSE_URI_EXTRA + extrasKeySuffix,
            drmConfiguration.licenseUri != null ? drmConfiguration.licenseUri.toString() : null);
    intent.putExtra(DRM_MULTI_SESSION_EXTRA + extrasKeySuffix, drmConfiguration.multiSession);
    intent.putExtra(
            DRM_FORCE_DEFAULT_LICENSE_URI_EXTRA + extrasKeySuffix,
            drmConfiguration.forceDefaultLicenseUri);

    String[] drmKeyRequestProperties = new String[drmConfiguration.requestHeaders.size() * 2];
    int index = 0;
    for (Map.Entry<String, String> entry : drmConfiguration.requestHeaders.entrySet()) {
      drmKeyRequestProperties[index++] = entry.getKey();
      drmKeyRequestProperties[index++] = entry.getValue();
    }
    intent.putExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA + extrasKeySuffix, drmKeyRequestProperties);

//    List<Integer> drmSessionForClearTypes = drmConfiguration.sessionForClearTypes;
    List<Integer> drmSessionForClearTypes = drmConfiguration.forcedSessionTrackTypes;
    if (drmSessionForClearTypes!=null&&drmSessionForClearTypes.size()==2) {
      // Only video and audio together are supported.
      checkState(
              drmSessionForClearTypes.size() == 2
                      && drmSessionForClearTypes.contains(C.TRACK_TYPE_VIDEO)
                      && drmSessionForClearTypes.contains(C.TRACK_TYPE_AUDIO));
      intent.putExtra(DRM_SESSION_FOR_CLEAR_CONTENT + extrasKeySuffix, true);
    }
  }

  private static void addClippingPropertiesToIntent(
          MediaItem.ClippingConfiguration clippingProperties, Intent intent, String extrasKeySuffix) {
    if (clippingProperties.startPositionMs != 0) {
      intent.putExtra(
              CLIP_START_POSITION_MS_EXTRA + extrasKeySuffix, clippingProperties.startPositionMs);
    }
    if (clippingProperties.endPositionMs != C.TIME_END_OF_SOURCE) {
      intent.putExtra(
              CLIP_END_POSITION_MS_EXTRA + extrasKeySuffix, clippingProperties.endPositionMs);
    }
  }
}
