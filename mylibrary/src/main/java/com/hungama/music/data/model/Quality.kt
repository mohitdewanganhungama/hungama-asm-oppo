package com.hungama.music.data.model

import com.hungama.music.HungamaMusicApp
import com.hungama.music.R

enum class Quality(val qualityName: String, val qualityPrefix:String, val id: Int,val serverKey:String) {
    AUTO(HungamaMusicApp.getInstance().getString(R.string.quality_1), "A",1,"auto"),
    HIGH(HungamaMusicApp.getInstance().getString(R.string.quality_2), "H",2,"high"),
    MEDIUM(HungamaMusicApp.getInstance().getString(R.string.quality_3), "M",3,"mid"),
    HD(HungamaMusicApp.getInstance().getString(R.string.quality_4), "HD",4,"hd"),
    DOLBY(HungamaMusicApp.getInstance().getString(R.string.quality_5), "D",5,"dolby"),
    PREVIEW(HungamaMusicApp.getInstance().getString(R.string.quality_6), "preview",6,"preview");

    companion object {

        @JvmStatic
        fun valueOf(value: Int): Quality {
            return when (value) {
                1 -> AUTO
                2 -> HIGH
                3 -> MEDIUM
                4 -> HD
                5 -> DOLBY
                6 -> PREVIEW
                else -> AUTO
            }
        }

        fun getQualityByName(qualityName: String): Quality {
            if (qualityName.contains(AUTO.qualityName)){
                return AUTO
            }else if (qualityName.contains(HIGH.qualityName)){
                return HIGH
            }else if (qualityName.contains(MEDIUM.qualityName)){
                return MEDIUM
            }else if (qualityName.contains(HD.qualityName)){
                return HD
            }else if (qualityName.contains(DOLBY.qualityName)){
                return DOLBY
            }else if (qualityName.contains(PREVIEW.qualityName)){
                return PREVIEW
            }else{
                return AUTO
            }
        }

        fun getServerKeyByName(qualityPrefix: String): String {
            if (qualityPrefix.equals(AUTO.qualityPrefix, true)){
                return AUTO.serverKey
            }else if (qualityPrefix.equals(HIGH.qualityPrefix, true)){
                return HIGH.serverKey
            }else if (qualityPrefix.equals(MEDIUM.qualityPrefix, true)){
                return MEDIUM.serverKey
            }else if (qualityPrefix.equals(HD.qualityPrefix, true)){
                return HD.serverKey
            }else if (qualityPrefix.equals(DOLBY.qualityPrefix, true)){
                return DOLBY.serverKey
            }else if (qualityPrefix.equals(PREVIEW.qualityPrefix, true)){
                return PREVIEW.serverKey
            }else{
                return AUTO.serverKey
            }
        }

    }
}