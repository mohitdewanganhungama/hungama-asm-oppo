package com.hungama.music.player.audioplayer

import com.hungama.music.player.audioplayer.player.AudioPlayer
import com.hungama.music.player.audioplayer.player.ExoPlayer
import com.hungama.music.player.audioplayer.repositories.ITrackRepository
import com.hungama.music.player.audioplayer.repositories.TrackRepository

/**
 * Enables Inversion of Control by depending on abstraction rather than concrete implementation.
 * Here we can pass FakeTrackRepository instead of the real one to test out the behavior of presenters.
 */
object Injection {

    fun provideTrackRepository(): ITrackRepository {
        return TrackRepository.getInstance()
    }

    fun provideAudioPlayer(): AudioPlayer {
        return ExoPlayer()
    }
}