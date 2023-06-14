package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class OnboardingConfigModel(
    @SerializedName("first_screen")
    var firstScreen: FirstScreen? = FirstScreen(),
    @SerializedName("fourth_screen")
    var fourthScreen: FourthScreen? = FourthScreen(),
    @SerializedName("second_screen")
    var secondScreen: SecondScreen? = SecondScreen(),
    @SerializedName("third_screen")
    var thirdScreen: ThirdScreen? = ThirdScreen()
) : Parcelable {
    @Keep
    @Parcelize
    data class FirstScreen(
        @SerializedName("number_of_session")
        var numberOfSession: Int? = 0,
        @SerializedName("screen")
        var screen: String? = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class FourthScreen(
        @SerializedName("number_of_session")
        var numberOfSession: Int? = 0,
        @SerializedName("screen")
        var screen: String? = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class SecondScreen(
        @SerializedName("number_of_session")
        var numberOfSession: Int? = 0,
        @SerializedName("screen")
        var screen: String? = ""
    ) : Parcelable

    @Keep
    @Parcelize
    data class ThirdScreen(
        @SerializedName("number_of_session")
        var numberOfSession: Int? = 0,
        @SerializedName("screen")
        var screen: String? = ""
    ) : Parcelable
}