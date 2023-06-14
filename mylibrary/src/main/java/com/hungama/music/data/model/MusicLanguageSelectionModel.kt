package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class MusicLanguageSelectionModel(
    @SerializedName("data")
    var data: Data? = Data()
) {
    @Keep
    data class Data(
        @SerializedName("body")
        var body: Body? = Body()
    ) {
        @Keep
        data class Body(
            @SerializedName("rows")
            var rows: List<Row?>? = listOf(),
            @SerializedName("transliteration")
            var transliteration: Transliteration? = Transliteration()
        ) {
            @Keep
            data class Row(
                @SerializedName("heading")
                var heading: String = "",
                @SerializedName("hscroll")
                var hscroll: Int? = 0,
                @SerializedName("id")
                var id: String? = null,
                @SerializedName("items")
                var items: List<Item?>? = listOf(),
                @SerializedName("more")
                var more: Int? = 0,
                @SerializedName("numrow")
                var numrow: Int? = 0,
                @SerializedName("subheadding")
                var subheadding: String = "",
                @SerializedName("type")
                var type: Int? = 0
            ) {
                @Keep
                data class Item(
                    @SerializedName("code")
                    var code: String? = "",
                    @SerializedName("title")
                    var title: String = "",
                    @SerializedName("configuration")
                    var configuration: String? = null,
                    @SerializedName("data")
                    var data: Data? = Data(),
                    @SerializedName("headding")
                    var headding: String = "",
                    @SerializedName("image")
                    var image: String = "",
                    @SerializedName("itype")
                    var itype: Int? = 0,
                    @SerializedName("sequence")
                    var sequence: Int? = 0,
                    @SerializedName("transliteration-key")
                    var transliterationKey: String? = null,
                    @SerializedName("type")
                    var type: String? = "",
                    var isSelected:Boolean=false
                ) {
                    @Keep
                    data class Data(
                        @SerializedName("description")
                        var description: String = "",
                        @SerializedName("id")
                        var id: String = "",
                        @SerializedName("image")
                        var image: String = "",
                        @SerializedName("imageFileSubTypeId")
                        var imageFileSubTypeId: Any? = Any(),
                        @SerializedName("misc")
                        var misc: Misc? = Misc(),
                        @SerializedName("releasedate")
                        var releasedate: String? = null,
                        @SerializedName("title")
                        var title: String = "",
                        @SerializedName("type")
                        var type: Int? = 0
                    ) {
                        @Keep
                        data class Misc(
                            @SerializedName("fav_count")
                            var favCount: Int? = 0,
                            @SerializedName("playcount")
                            var playcount: Int? = 0
                        )
                    }
                }
            }

            @Keep
            data class Transliteration(
                @SerializedName("asklater")
                var asklater: Asklater? = Asklater(),
                @SerializedName("headding")
                var headding: Headding? = Headding(),
                @SerializedName("save")
                var save: Save? = Save(),
                @SerializedName("subheadding")
                var subheadding: Subheadding? = Subheadding()
            ) {
                @Keep
                data class Asklater(
                    @SerializedName("bn")
                    var bn: String = "",
                    @SerializedName("en")
                    var en: String = "",
                    @SerializedName("gu")
                    var gu: String = "",
                    @SerializedName("hi")
                    var hi: String = "",
                    @SerializedName("kn")
                    var kn: String = "",
                    @SerializedName("ml")
                    var ml: String = "",
                    @SerializedName("mr")
                    var mr: String = "",
                    @SerializedName("or")
                    var or: String = "",
                    @SerializedName("pa")
                    var pa: String = "",
                    @SerializedName("ta")
                    var ta: String = "",
                    @SerializedName("te")
                    var te: String = "",
                    @SerializedName("ur")
                    var ur: String = ""
                )

                @Keep
                data class Headding(
                    @SerializedName("bn")
                    var bn: String = "",
                    @SerializedName("en")
                    var en: String = "",
                    @SerializedName("gu")
                    var gu: String = "",
                    @SerializedName("hi")
                    var hi: String = "",
                    @SerializedName("kn")
                    var kn: String = "",
                    @SerializedName("ml")
                    var ml: String = "",
                    @SerializedName("mr")
                    var mr: String = "",
                    @SerializedName("or")
                    var or: String = "",
                    @SerializedName("pa")
                    var pa: String = "",
                    @SerializedName("ta")
                    var ta: String = "",
                    @SerializedName("te")
                    var te: String = "",
                    @SerializedName("ur")
                    var ur: String = ""
                )

                @Keep
                data class Save(
                    @SerializedName("bn")
                    var bn: String = "",
                    @SerializedName("en")
                    var en: String = "",
                    @SerializedName("gu")
                    var gu: String = "",
                    @SerializedName("hi")
                    var hi: String = "",
                    @SerializedName("kn")
                    var kn: String = "",
                    @SerializedName("ml")
                    var ml: String = "",
                    @SerializedName("mr")
                    var mr: String = "",
                    @SerializedName("or")
                    var or: String = "",
                    @SerializedName("pa")
                    var pa: String = "",
                    @SerializedName("ta")
                    var ta: String = "",
                    @SerializedName("te")
                    var te: String = "",
                    @SerializedName("ur")
                    var ur: String = ""
                )

                @Keep
                data class Subheadding(
                    @SerializedName("bn")
                    var bn: String = "",
                    @SerializedName("en")
                    var en: String = "",
                    @SerializedName("gu")
                    var gu: String = "",
                    @SerializedName("hi")
                    var hi: String = "",
                    @SerializedName("kn")
                    var kn: String = "",
                    @SerializedName("ml")
                    var ml: String = "",
                    @SerializedName("mr")
                    var mr: String = "",
                    @SerializedName("or")
                    var or: String = "",
                    @SerializedName("pa")
                    var pa: String = "",
                    @SerializedName("ta")
                    var ta: String = "",
                    @SerializedName("te")
                    var te: String = "",
                    @SerializedName("ur")
                    var ur: String = ""
                )
            }
        }
    }
}