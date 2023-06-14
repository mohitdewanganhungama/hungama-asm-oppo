package com.hungama.music.data.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class UserOrdersModel(
    @SerializedName("orders")
    var orders: ArrayList<Order> = ArrayList()
) : Parcelable {
    @Keep
    @Parcelize
    data class Order(
        @SerializedName("admin_graphql_api_id")
        var adminGraphqlApiId: String? = "",
        @SerializedName("app_id")
        var appId: Int? = 0,
        @SerializedName("billing_address")
        var billingAddress: BillingAddress? = BillingAddress(),
        @SerializedName("browser_ip")
        var browserIp: @RawValue Any? = Any(),
        @SerializedName("buyer_accepts_marketing")
        var buyerAcceptsMarketing: Boolean? = false,
        @SerializedName("cancel_reason")
        var cancelReason: @RawValue Any? = Any(),
        @SerializedName("cancelled_at")
        var cancelledAt: @RawValue Any? = Any(),
        @SerializedName("cart_token")
        var cartToken: @RawValue Any? = Any(),
        @SerializedName("checkout_id")
        var checkoutId: @RawValue Any? = Any(),
        @SerializedName("checkout_token")
        var checkoutToken: @RawValue Any? = Any(),
        @SerializedName("closed_at")
        var closedAt: @RawValue Any? = Any(),
        @SerializedName("confirmed")
        var confirmed: Boolean? = false,
        @SerializedName("contact_email")
        var contactEmail: String? = "",
        @SerializedName("created_at")
        var createdAt: String? = "",
        @SerializedName("currency")
        var currency: String? = "",
        @SerializedName("current_subtotal_price")
        var currentSubtotalPrice: String? = "",
        @SerializedName("current_subtotal_price_set")
        var currentSubtotalPriceSet: CurrentSubtotalPriceSet? = CurrentSubtotalPriceSet(),
        @SerializedName("current_total_discounts")
        var currentTotalDiscounts: String? = "",
        @SerializedName("current_total_discounts_set")
        var currentTotalDiscountsSet: CurrentTotalDiscountsSet? = CurrentTotalDiscountsSet(),
        @SerializedName("current_total_duties_set")
        var currentTotalDutiesSet: @RawValue Any? = Any(),
        @SerializedName("current_total_price")
        var currentTotalPrice: String? = "",
        @SerializedName("current_total_price_set")
        var currentTotalPriceSet: CurrentTotalPriceSet? = CurrentTotalPriceSet(),
        @SerializedName("current_total_tax")
        var currentTotalTax: String? = "",
        @SerializedName("current_total_tax_set")
        var currentTotalTaxSet: CurrentTotalTaxSet? = CurrentTotalTaxSet(),
        @SerializedName("customer")
        var customer: Customer? = Customer(),
        @SerializedName("customer_locale")
        var customerLocale: @RawValue Any? = Any(),
        @SerializedName("device_id")
        var deviceId: @RawValue Any? = Any(),
        @SerializedName("discount_applications")
        var discountApplications: @RawValue List<Any?>? = listOf(),
        @SerializedName("discount_codes")
        var discountCodes: @RawValue List<Any?>? = listOf(),
        @SerializedName("email")
        var email: String? = "",
        @SerializedName("estimated_taxes")
        var estimatedTaxes: Boolean? = false,
        @SerializedName("financial_status")
        var financialStatus: String? = "",
        @SerializedName("fulfillment_status")
        var fulfillmentStatus: @RawValue Any? = Any(),
        @SerializedName("fulfillments")
        var fulfillments: @RawValue List<Any?>? = listOf(),
        @SerializedName("gateway")
        var gateway: String? = "",
        @SerializedName("id")
        var id: Long? = 0,
        @SerializedName("landing_site")
        var landingSite: @RawValue Any? = Any(),
        @SerializedName("landing_site_ref")
        var landingSiteRef: @RawValue Any? = Any(),
        @SerializedName("line_items")
        var lineItems: List<LineItem> = listOf(),
        @SerializedName("location_id")
        var locationId: @RawValue Any? = Any(),
        @SerializedName("name")
        var name: String? = "",
        @SerializedName("note")
        var note: @RawValue Any? = Any(),
        @SerializedName("note_attributes")
        var noteAttributes: @RawValue List<Any?>? = listOf(),
        @SerializedName("number")
        var number: Int? = 0,
        @SerializedName("order_number")
        var orderNumber: Int? = 0,
        @SerializedName("order_status_url")
        var orderStatusUrl: String? = "",
        @SerializedName("original_total_duties_set")
        var originalTotalDutiesSet: @RawValue Any? = Any(),
        @SerializedName("payment_gateway_names")
        var paymentGatewayNames: @RawValue List<Any?>? = listOf(),
        @SerializedName("payment_terms")
        var paymentTerms: @RawValue Any? = Any(),
        @SerializedName("phone")
        var phone: @RawValue Any? = Any(),
        @SerializedName("presentment_currency")
        var presentmentCurrency: String? = "",
        @SerializedName("processed_at")
        var processedAt: String? = "",
        @SerializedName("processing_method")
        var processingMethod: String? = "",
        @SerializedName("reference")
        var reference: @RawValue Any? = Any(),
        @SerializedName("referring_site")
        var referringSite: @RawValue Any? = Any(),
        @SerializedName("refunds")
        var refunds: @RawValue List<Any?>? = listOf(),
        @SerializedName("shipping_address")
        var shippingAddress: ShippingAddress? = ShippingAddress(),
        @SerializedName("shipping_lines")
        var shippingLines: @RawValue List<Any?>? = listOf(),
        @SerializedName("source_identifier")
        var sourceIdentifier: @RawValue Any? = Any(),
        @SerializedName("source_name")
        var sourceName: String? = "",
        @SerializedName("source_url")
        var sourceUrl: @RawValue Any? = Any(),
        @SerializedName("subtotal_price")
        var subtotalPrice: String? = "",
        @SerializedName("subtotal_price_set")
        var subtotalPriceSet: SubtotalPriceSet? = SubtotalPriceSet(),
        @SerializedName("tags")
        var tags: String? = "",
        @SerializedName("tax_lines")
        var taxLines: @RawValue List<Any?>? = listOf(),
        @SerializedName("taxes_included")
        var taxesIncluded: Boolean? = false,
        @SerializedName("test")
        var test: Boolean? = false,
        @SerializedName("token")
        var token: String? = "",
        @SerializedName("total_discounts")
        var totalDiscounts: String? = "",
        @SerializedName("total_discounts_set")
        var totalDiscountsSet: TotalDiscountsSet? = TotalDiscountsSet(),
        @SerializedName("total_line_items_price")
        var totalLineItemsPrice: String? = "",
        @SerializedName("total_line_items_price_set")
        var totalLineItemsPriceSet: TotalLineItemsPriceSet? = TotalLineItemsPriceSet(),
        @SerializedName("total_outstanding")
        var totalOutstanding: String? = "",
        @SerializedName("total_price")
        var totalPrice: String? = "",
        @SerializedName("total_price_set")
        var totalPriceSet: TotalPriceSet? = TotalPriceSet(),
        @SerializedName("total_price_usd")
        var totalPriceUsd: String? = "",
        @SerializedName("total_shipping_price_set")
        var totalShippingPriceSet: TotalShippingPriceSet? = TotalShippingPriceSet(),
        @SerializedName("total_tax")
        var totalTax: String? = "",
        @SerializedName("total_tax_set")
        var totalTaxSet: TotalTaxSet? = TotalTaxSet(),
        @SerializedName("total_tip_received")
        var totalTipReceived: String? = "",
        @SerializedName("total_weight")
        var totalWeight: Int? = 0,
        @SerializedName("updated_at")
        var updatedAt: String? = "",
        @SerializedName("user_id")
        var userId: @RawValue Any? = Any()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class BillingAddress(
            @SerializedName("address1")
            var address1: String? = "",
            @SerializedName("address2")
            var address2: @RawValue Any? = Any(),
            @SerializedName("city")
            var city: String? = "",
            @SerializedName("company")
            var company: @RawValue Any? = Any(),
            @SerializedName("country")
            var country: String? = "",
            @SerializedName("country_code")
            var countryCode: String? = "",
            @SerializedName("first_name")
            var firstName: String? = "",
            @SerializedName("last_name")
            var lastName: String? = "",
            @SerializedName("latitude")
            var latitude: Double? = 0.0,
            @SerializedName("longitude")
            var longitude: Double? = 0.0,
            @SerializedName("name")
            var name: String? = "",
            @SerializedName("phone")
            var phone: String? = "",
            @SerializedName("province")
            var province: String? = "",
            @SerializedName("province_code")
            var provinceCode: String? = "",
            @SerializedName("zip")
            var zip: String? = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class CurrentSubtotalPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class CurrentTotalDiscountsSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class CurrentTotalPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class CurrentTotalTaxSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class Customer(
            @SerializedName("accepts_marketing")
            var acceptsMarketing: Boolean? = false,
            @SerializedName("accepts_marketing_updated_at")
            var acceptsMarketingUpdatedAt: String? = "",
            @SerializedName("admin_graphql_api_id")
            var adminGraphqlApiId: String? = "",
            @SerializedName("created_at")
            var createdAt: String? = "",
            @SerializedName("currency")
            var currency: String? = "",
            @SerializedName("default_address")
            var defaultAddress: DefaultAddress? = DefaultAddress(),
            @SerializedName("email")
            var email: String? = "",
            @SerializedName("first_name")
            var firstName: @RawValue Any? = Any(),
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("last_name")
            var lastName: @RawValue Any? = Any(),
            @SerializedName("last_order_id")
            var lastOrderId: Long? = 0,
            @SerializedName("last_order_name")
            var lastOrderName: String? = "",
            @SerializedName("marketing_opt_in_level")
            var marketingOptInLevel: @RawValue Any? = Any(),
            @SerializedName("multipass_identifier")
            var multipassIdentifier: @RawValue Any? = Any(),
            @SerializedName("note")
            var note: @RawValue Any? = Any(),
            @SerializedName("orders_count")
            var ordersCount: Int? = 0,
            @SerializedName("phone")
            var phone: @RawValue Any? = Any(),
            @SerializedName("sms_marketing_consent")
            var smsMarketingConsent: @RawValue Any? = Any(),
            @SerializedName("state")
            var state: String? = "",
            @SerializedName("tags")
            var tags: String? = "",
            @SerializedName("tax_exempt")
            var taxExempt: Boolean? = false,
            @SerializedName("tax_exemptions")
            var taxExemptions: @RawValue List<Any?>? = listOf(),
            @SerializedName("total_spent")
            var totalSpent: String? = "",
            @SerializedName("updated_at")
            var updatedAt: String? = "",
            @SerializedName("verified_email")
            var verifiedEmail: Boolean? = false
        ) : Parcelable {
            @Keep
            @Parcelize
            data class DefaultAddress(
                @SerializedName("address1")
                var address1: String? = "",
                @SerializedName("address2")
                var address2: String? = "",
                @SerializedName("city")
                var city: String? = "",
                @SerializedName("company")
                var company: String? = "",
                @SerializedName("country")
                var country: String? = "",
                @SerializedName("country_code")
                var countryCode: String? = "",
                @SerializedName("country_name")
                var countryName: String? = "",
                @SerializedName("customer_id")
                var customerId: Long? = 0,
                @SerializedName("default")
                var default: Boolean? = false,
                @SerializedName("first_name")
                var firstName: String? = "",
                @SerializedName("id")
                var id: Long? = 0,
                @SerializedName("last_name")
                var lastName: String? = "",
                @SerializedName("name")
                var name: String? = "",
                @SerializedName("phone")
                var phone: String? = "",
                @SerializedName("province")
                var province: String? = "",
                @SerializedName("province_code")
                var provinceCode: String? = "",
                @SerializedName("zip")
                var zip: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class LineItem(
            @SerializedName("admin_graphql_api_id")
            var adminGraphqlApiId: String? = "",
            @SerializedName("discount_allocations")
            var discountAllocations: @RawValue List<Any?>? = listOf(),
            @SerializedName("duties")
            var duties: @RawValue List<Any?>? = listOf(),
            @SerializedName("fulfillable_quantity")
            var fulfillableQuantity: Int? = 0,
            @SerializedName("fulfillment_service")
            var fulfillmentService: String? = "",
            @SerializedName("fulfillment_status")
            var fulfillmentStatus: @RawValue Any? = Any(),
            @SerializedName("gift_card")
            var giftCard: Boolean? = false,
            @SerializedName("grams")
            var grams: Int? = 0,
            @SerializedName("id")
            var id: Long? = 0,
            @SerializedName("name")
            var name: String? = "",
            @SerializedName("price")
            var price: String? = "",
            @SerializedName("price_set")
            var priceSet: PriceSet? = PriceSet(),
            @SerializedName("product_exists")
            var productExists: Boolean? = false,
            @SerializedName("product_id")
            var productId: Long? = 0,
            @SerializedName("properties")
            var properties: @RawValue List<Any?>? = listOf(),
            @SerializedName("quantity")
            var quantity: Int? = 0,
            @SerializedName("requires_shipping")
            var requiresShipping: Boolean? = false,
            @SerializedName("sku")
            var sku: @RawValue Any? = Any(),
            @SerializedName("tax_lines")
            var taxLines: @RawValue List<Any?>? = listOf(),
            @SerializedName("taxable")
            var taxable: Boolean? = false,
            @SerializedName("title")
            var title: String? = "",
            @SerializedName("total_discount")
            var totalDiscount: String? = "",
            @SerializedName("total_discount_set")
            var totalDiscountSet: TotalDiscountSet? = TotalDiscountSet(),
            @SerializedName("variant_id")
            var variantId: @RawValue Any? = Any(),
            @SerializedName("variant_inventory_management")
            var variantInventoryManagement: @RawValue Any? = Any(),
            @SerializedName("variant_title")
            var variantTitle: @RawValue Any? = Any(),
            @SerializedName("vendor")
            var vendor: @RawValue Any? = Any(),
            @SerializedName("body_html")
            var bodyHtml: String? = "",
            @SerializedName("image")
            var image: ProductRespModel.Product.Image? = ProductRespModel.Product.Image(),
            @SerializedName("images")
            var images: List<ProductRespModel.Product.Images?>? = listOf(),
            @SerializedName("product_type")
            var productType: String? = "",
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PriceSet(
                @SerializedName("presentment_money")
                var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
                @SerializedName("shop_money")
                var shopMoney: ShopMoney? = ShopMoney()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class PresentmentMoney(
                    @SerializedName("amount")
                    var amount: String? = "",
                    @SerializedName("currency_code")
                    var currencyCode: String? = ""
                ) : Parcelable

                @Keep
                @Parcelize
                data class ShopMoney(
                    @SerializedName("amount")
                    var amount: String? = "",
                    @SerializedName("currency_code")
                    var currencyCode: String? = ""
                ) : Parcelable
            }

            @Keep
            @Parcelize
            data class TotalDiscountSet(
                @SerializedName("presentment_money")
                var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
                @SerializedName("shop_money")
                var shopMoney: ShopMoney? = ShopMoney()
            ) : Parcelable {
                @Keep
                @Parcelize
                data class PresentmentMoney(
                    @SerializedName("amount")
                    var amount: String? = "",
                    @SerializedName("currency_code")
                    var currencyCode: String? = ""
                ) : Parcelable

                @Keep
                @Parcelize
                data class ShopMoney(
                    @SerializedName("amount")
                    var amount: String? = "",
                    @SerializedName("currency_code")
                    var currencyCode: String? = ""
                ) : Parcelable
            }
        }

        @Keep
        @Parcelize
        data class ShippingAddress(
            @SerializedName("address1")
            var address1: String? = "",
            @SerializedName("address2")
            var address2: String? = "",
            @SerializedName("city")
            var city: String? = "",
            @SerializedName("state")
            var state: String? = "",
            @SerializedName("company")
            var company: String? = "",
            @SerializedName("country")
            var country: String? = "",
            @SerializedName("country_code")
            var countryCode: String? = "",
            @SerializedName("first_name")
            var firstName: String? = "",
            @SerializedName("last_name")
            var lastName: String? = "",
            @SerializedName("latitude")
            var latitude: Double? = 0.0,
            @SerializedName("longitude")
            var longitude: Double? = 0.0,
            @SerializedName("name")
            var name: String? = "",
            @SerializedName("phone")
            var phone: String? = "",
            @SerializedName("province")
            var province: String? = "",
            @SerializedName("province_code")
            var provinceCode: String? = "",
            @SerializedName("zip")
            var zip: String? = ""
        ) : Parcelable

        @Keep
        @Parcelize
        data class SubtotalPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class TotalDiscountsSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class TotalLineItemsPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class TotalPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class TotalShippingPriceSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class TotalTaxSet(
            @SerializedName("presentment_money")
            var presentmentMoney: PresentmentMoney? = PresentmentMoney(),
            @SerializedName("shop_money")
            var shopMoney: ShopMoney? = ShopMoney()
        ) : Parcelable {
            @Keep
            @Parcelize
            data class PresentmentMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable

            @Keep
            @Parcelize
            data class ShopMoney(
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("currency_code")
                var currencyCode: String? = ""
            ) : Parcelable
        }
    }
}