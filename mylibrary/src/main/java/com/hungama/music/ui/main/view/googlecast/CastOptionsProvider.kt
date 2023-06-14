/*
 * Copyright 2019 Google LLC. All Rights Reserved.
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
package com.hungama.music.ui.main.view.googlecast

import android.content.Context
import android.text.format.DateUtils
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.*
import com.google.android.gms.common.images.WebImage
import com.hungama.music.R
import java.util.*

/**
 * Implements [OptionsProvider] to provide [CastOptions].
 */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val supportedNamespaces: MutableList<String> = ArrayList()
        supportedNamespaces.add(CUSTOM_NAMESPACE)
        val notificationOptions = NotificationOptions.Builder()
            .setActions(
                Arrays.asList(
                    MediaIntentReceiver.ACTION_FORWARD,
                    MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                    MediaIntentReceiver.ACTION_REWIND,
                    MediaIntentReceiver.ACTION_STOP_CASTING
                ), intArrayOf(1, 3)
            )
            .setSkipStepMs(30 * DateUtils.SECOND_IN_MILLIS)
            .setTargetActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setImagePicker(ImagePickerImpl())
            .setMediaSessionEnabled(true)
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()
        /** Following lines enable Cast Connect  */
        val launchOptions = LaunchOptions.Builder()
            .setAndroidReceiverCompatible(true)
            .build()
        return CastOptions.Builder()
            .setLaunchOptions(launchOptions)
            .setReceiverApplicationId(context.getString(R.string.chormecast_app_id))
            .setCastMediaOptions(mediaOptions)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(appContext: Context): List<SessionProvider>? {
        return ArrayList()
    }

    private class ImagePickerImpl : ImagePicker() {
        override fun onPickImage(mediaMetadata: MediaMetadata?, hints: ImageHints): WebImage? {
            val type = hints.type
            if (mediaMetadata == null || !mediaMetadata.hasImages()) {
                return null
            }
            val images = mediaMetadata.images
            return if (images.size == 1) {
                images[0]
            } else {
                if (type == IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    images[0]
                } else {
                    images[1]
                }
            }
        }
    }

    companion object {
        const val CUSTOM_NAMESPACE = "urn:x-cast:custom_namespace"
    }
}