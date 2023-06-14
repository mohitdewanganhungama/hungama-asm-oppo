package com.hungama.music.data.model//package com.hungama.music.chart.model
//
//
//import com.google.gson.annotations.SerializedName
//import androidx.annotation.Keep
//
//@Keep
//data class MovieModel(
//    @SerializedName("body")
//    var body: Body? = Body()
//) {
//    @Keep
//    data class Body(
//        @SerializedName("artist")
//        var artist: List<String?>? = null,
//        @SerializedName("description")
//        var description: String? = "",
//        @SerializedName("genre")
//        var genre: List<String?>? = null,
//        @SerializedName("id")
//        var id: String? = "",
//        @SerializedName("image")
//        var image: String? = "",
//        @SerializedName("imageFileSubTypeId")
//        var imageFileSubTypeId: String? = "0",
//        @SerializedName("items")
//        var items: List<Item?>? = null,
//        @SerializedName("itype")
//        var itype: Int? = 0,
//        @SerializedName("language")
//        var language: List<String?>? = null,
//        @SerializedName("releasedate")
//        var releasedate: String? = "",
//        @SerializedName("similar")
//        var similar: List<Similar?>? = null,
//        @SerializedName("title")
//        var title: String? = "",
//        @SerializedName("trailers")
//        var trailers: List<Trailers?>? = null,
//        @SerializedName("castcrew")
//        var castCrew: List<CastCrew?>? = null,
//        @SerializedName("watched")
//        var watched: List<Watched?>? = null,
//    ) {
//        @Keep
//        data class Item(
//            @SerializedName("data")
//            var `data`: Data? = Data(),
//            @SerializedName("itype")
//            var itype: Int? = 0
//        ) {
//            @Keep
//            data class Data(
//                @SerializedName("artist")
//                var artist: List<String?>? = null,
//                @SerializedName("description")
//                var description: String? = "",
//                @SerializedName("genre")
//                var genre: List<String?>? = null,
//                @SerializedName("id")
//                var id: String? = "",
//                @SerializedName("image")
//                var image: String? = "",
//                @SerializedName("imageFileSubTypeId")
//                var imageFileSubTypeId: String? = "0",
//                @SerializedName("language")
//                var language: List<String?>? = null,
//                @SerializedName("releasedate")
//                var releasedate: String? = "",
//                @SerializedName("title")
//                var title: String? = null
//            )
//        }
//
//        @Keep
//        data class Similar(
//            @SerializedName("data")
//            var `data`: Data? = Data(),
//            @SerializedName("itype")
//            var itype: Int? = 0
//        ) {
//            @Keep
//            data class Data(
//                @SerializedName("artist")
//                var artist: List<String?>? = null,
//                @SerializedName("description")
//                var description: String? = "",
//                @SerializedName("genre")
//                var genre: List<String?>? = null,
//                @SerializedName("id")
//                var id: String? = "",
//                @SerializedName("image")
//                var image: String? = "",
//                @SerializedName("imageFileSubTypeId")
//                var imageFileSubTypeId: String? = "0",
//                @SerializedName("language")
//                var language: List<Any?>? = null,
//                @SerializedName("releasedate")
//                var releasedate: String? = "",
//                @SerializedName("title")
//                var title: String? = null
//            )
//        }
//
//        @Keep
//        data class Trailers(
//            @SerializedName("data")
//            var `data`: Data? = Data(),
//            @SerializedName("itype")
//            var itype: Int? = 0
//        ) {
//            @Keep
//            data class Data(
//                @SerializedName("artist")
//                var artist: List<Any?>? = null,
//                @SerializedName("description")
//                var description: String? = "",
//                @SerializedName("genre")
//                var genre: List<String?>? = null,
//                @SerializedName("id")
//                var id: String? = "",
//                @SerializedName("image")
//                var image: String? = "",
//                @SerializedName("imageFileSubTypeId")
//                var imageFileSubTypeId: String? = "0",
//                @SerializedName("language")
//                var language: List<Any?>? = null,
//                @SerializedName("releasedate")
//                var releasedate: String? = "",
//                @SerializedName("title")
//                var title: String? = null
//            )
//        }
//
//        @Keep
//        data class CastCrew(
//            @SerializedName("data")
//            var `data`: Data? = Data(),
//            @SerializedName("itype")
//            var itype: Int? = 0
//        ) {
//            @Keep
//            data class Data(
//                @SerializedName("artist")
//                var artist: List<Any?>? = null,
//                @SerializedName("description")
//                var description: String? = "",
//                @SerializedName("genre")
//                var genre: List<String?>? = null,
//                @SerializedName("id")
//                var id: String? = "",
//                @SerializedName("image")
//                var image: String? = "",
//                @SerializedName("imageFileSubTypeId")
//                var imageFileSubTypeId: String? = "0",
//                @SerializedName("language")
//                var language: List<Any?>? = null,
//                @SerializedName("releasedate")
//                var releasedate: String? = "",
//                @SerializedName("title")
//                var title: String? = null
//            )
//        }
//
//        @Keep
//        data class Watched(
//            @SerializedName("data")
//            var `data`: Data? = Data(),
//            @SerializedName("itype")
//            var itype: Int? = 0
//        ) {
//            @Keep
//            data class Data(
//                @SerializedName("artist")
//                var artist: List<Any?>? = null,
//                @SerializedName("description")
//                var description: String? = "",
//                @SerializedName("genre")
//                var genre: List<String?>? = null,
//                @SerializedName("id")
//                var id: String? = "",
//                @SerializedName("image")
//                var image: String? = "",
//                @SerializedName("imageFileSubTypeId")
//                var imageFileSubTypeId: String? = "0",
//                @SerializedName("language")
//                var language: List<Any?>? = null,
//                @SerializedName("releasedate")
//                var releasedate: String? = "",
//                @SerializedName("title")
//                var title: String? = null
//            )
//        }
//    }
//}