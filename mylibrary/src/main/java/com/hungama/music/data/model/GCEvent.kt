package com.hungama.music.data.model

import com.hungama.music.eventanalytic.EventConstant

enum class GCEvent(val gcEventName: String, val eventName: String) {
    GC_STREAM("gc_stream", "Stream"),
    GC_ADDED_TO_PLAYLIST("gc_added_to_playlist", "Added to Playlist"),
    GC_LOGIN_SUCCESS("gc_login_success", "Login - Success"),
    GC_RENT_OPEN_RENT_PAGE("gc_rent_open_rent_page", "Rent - open Rent Page"),
    GC_ADDED_TO_WATCHLIST("gc_added_to_watchlist", "Added To WatchList"),
    GC_LANGUAGE_CHANGED("gc_language_changed", "Language Changed"),
    GC_APP_LAUNCH("gc_app_launch", "App Launch"),
    GC_LANGUAGE_SELECTED("gc_language_selected", "Language Selected"),
    GC_APP_LANGUAGE_SELECTED("gc_app_language_selected", "app_language_selected"),
    GC_CREATED_PLAYLIST("gc_created_playlist", "Created Playlist"),
    GC_MOVIES_LANGUAGE_SELECTED("gc_movies_language_selected", "movies_language_selected"),
    GC_REGISTRATION_SUCCESS("gc_registration_success", "Registration - Success"),
    GC_FAVOURITED("gc_favourited", "Favourited");

    companion object {
        fun getGCEventName(eventName: String): String {
            if (EventConstant.gamiFicationStreamActions.contains(eventName)){
                return GCEvent.GC_STREAM.gcEventName
            }else if (eventName.contains(GCEvent.GC_STREAM.eventName)){
                return GCEvent.GC_STREAM.gcEventName
            }else  if (eventName.contains(GCEvent.GC_ADDED_TO_PLAYLIST.eventName)){
                return GCEvent.GC_ADDED_TO_PLAYLIST.gcEventName
            }else  if (eventName.contains(GCEvent.GC_LOGIN_SUCCESS.eventName)){
                return GCEvent.GC_LOGIN_SUCCESS.gcEventName
            }else  if (eventName.contains(GCEvent.GC_RENT_OPEN_RENT_PAGE.eventName)){
                return GCEvent.GC_RENT_OPEN_RENT_PAGE.gcEventName
            }else  if (eventName.contains(GCEvent.GC_ADDED_TO_WATCHLIST.eventName)){
                return GCEvent.GC_ADDED_TO_WATCHLIST.gcEventName
            }else  if (eventName.contains(GCEvent.GC_LANGUAGE_CHANGED.eventName)){
                return GCEvent.GC_LANGUAGE_CHANGED.gcEventName
            }else  if (eventName.contains(GCEvent.GC_APP_LAUNCH.eventName)){
                return GCEvent.GC_APP_LAUNCH.gcEventName
            }else  if (eventName.contains(GCEvent.GC_LANGUAGE_SELECTED.eventName)){
                return GCEvent.GC_LANGUAGE_SELECTED.gcEventName
            }else  if (eventName.contains(GCEvent.GC_APP_LANGUAGE_SELECTED.eventName)){
                return GCEvent.GC_APP_LANGUAGE_SELECTED.gcEventName
            }else  if (eventName.contains(GCEvent.GC_CREATED_PLAYLIST.eventName)){
                return GCEvent.GC_CREATED_PLAYLIST.gcEventName
            }else  if (eventName.contains(GCEvent.GC_MOVIES_LANGUAGE_SELECTED.eventName)){
                return GCEvent.GC_MOVIES_LANGUAGE_SELECTED.gcEventName
            }else  if (eventName.contains(GCEvent.GC_FAVOURITED.eventName)){
                return GCEvent.GC_FAVOURITED.gcEventName
            }else  if (eventName.contains(GCEvent.GC_REGISTRATION_SUCCESS.eventName)){
                return GCEvent.GC_REGISTRATION_SUCCESS.gcEventName
            }else{
                return ""
            }
        }

    }
}