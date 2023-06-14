package com.hungama.fetch2

import com.hungama.fetch2core.FetchObserver
import com.hungama.fetch2core.Reason

/**
 * Fetch observer for groups. This observer also specifies which download
 * triggers the onChanged in the group.
 * */
interface FetchGroupObserver: FetchObserver<List<Download>> {

    /**
     * Method called when the download list has changed.
     * @param data the download list.
     * @param triggerDownload the download that triggered the change.
     * @param reason the reason why onChanged was called for the triggered download.
     * */
    fun onChanged(data: List<Download>, triggerDownload: Download, reason: Reason)

}