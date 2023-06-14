package com.hungama.fetch2.helper

import com.hungama.fetch2.database.DownloadInfo
import com.hungama.fetch2.database.FetchDatabaseManagerWrapper


class DownloadInfoUpdater(private val fetchDatabaseManagerWrapper: FetchDatabaseManagerWrapper) {

    fun updateFileBytesInfoAndStatusOnly(downloadInfo: DownloadInfo) {
        fetchDatabaseManagerWrapper.updateFileBytesInfoAndStatusOnly(downloadInfo)
    }

    fun update(downloadInfo: DownloadInfo) {
        fetchDatabaseManagerWrapper.update(downloadInfo)
    }

    fun getNewDownloadInfoInstance(): DownloadInfo {
        return fetchDatabaseManagerWrapper.getNewDownloadInfoInstance()
    }

}