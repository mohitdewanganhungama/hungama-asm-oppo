package com.hungama.music.data.model


import java.io.Serializable

data class ShippingDetailModel(var firstName: String? = "",
                               var lastName: String? = "",
                               var mobile: String? = "",
                               var pincode: String? = "",
                               var address1: String? = "",
                               var address2: String? = "",
                               var city: String? = "",
                               var state: String? = "",
                               var isPhisicalProduct: Boolean = false):Serializable
