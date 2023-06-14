package com.hungama.music.data.model

enum class PlanTypes(val planTypeName: String, val value: Int) {
    SUBSCRIPTION("SUBSCRIPTION", 0),
    RENTAL("RENTAL", 1),
    EVENT("EVENT", 2),
    FREE("FREE", 3);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): PlanTypes {
            return when (value) {
                0 -> SUBSCRIPTION
                1 -> RENTAL
                2 -> EVENT
                3 -> FREE
                else -> SUBSCRIPTION
            }
        }

    }
}