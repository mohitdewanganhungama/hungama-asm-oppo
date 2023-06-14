package com.hungama.music.data.model

enum class PlanNames(val planTypeName: String, val value: Int) {
    NONE("NONE", 0),
    TVOD("TVOD", 1),
    PTVOD("PTVOD", 2),
    SVOD("SVOD", 3),
    FVOD("FVOD", 4),
    AVOD("AVOD", 5),
    CVOD("CVOD", 6),
    PCVOD("PCVOD", 7),
    LE("LE", 8);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): PlanNames {
            return when (value) {
                1 -> TVOD
                2 -> PTVOD
                3 -> SVOD
                4 -> FVOD
                5 -> AVOD
                6 -> CVOD
                7 -> PCVOD
                8 -> LE
                else -> NONE
            }
        }

    }
}