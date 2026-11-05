package com.digitalwhy.kasir

import com.google.gson.annotations.SerializedName

data class Product(
    val id: String = "0",
    val barcode: String? = null,
    val name: String? = null,
    val price: Double? = 0.0,
    val stock: Double? = 0.0,
    @SerializedName(value = "costPrice", alternate = ["cost_price"])
    val costPrice: Double? = 0.0,
    @SerializedName(value = "expiryDate", alternate = ["expiry_date"])
    val expiryDate: String? = null,
    val type: String = "barcode",
    val icon: String = "package"
)
