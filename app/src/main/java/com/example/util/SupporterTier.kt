package com.example.util

/**
 * Google Play In-App Purchase Voluntary Supporter Tiers.
 * Tier 1 ($1.99), Tier 2 ($4.99), Tier 3 ($9.99), Tier 4 ($19.99).
 */
enum class SupporterTier(
    val id: String,
    val title: String,
    val priceUsd: String,
    val perkDescription: String
) {
    TIER_1(
        id = "tier_1",
        title = "Supporter (Tier 1)",
        priceUsd = "$1.99",
        perkDescription = "Buy the dev a coffee • Unlock Golden Supporter Badge"
    ),
    TIER_2(
        id = "tier_2",
        title = "Advocate (Tier 2)",
        priceUsd = "$4.99",
        perkDescription = "Fund high-SNR news curation & independent server costs"
    ),
    TIER_3(
        id = "tier_3",
        title = "Champion (Tier 3)",
        priceUsd = "$9.99",
        perkDescription = "Champion open-source, ad-free journalism & AI models"
    ),
    TIER_4(
        id = "tier_4",
        title = "Benefactor (Tier 4)",
        priceUsd = "$19.99",
        perkDescription = "Lifetime indie development benefactor & priority recognition"
    )
}
