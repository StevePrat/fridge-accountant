package io.initialcapacity.datamodel

import java.sql.Date
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date as UtilDate

data class FridgeAnalysis(
    val fridgeId: Long,
    val fridgeName: String,
    val totalSlots: Int,
    val occupiedSlots: Int,
    val freeSlots: Int,
    val occupancyPercentage: Int,
    val ownerUsage: List<OwnerUsage>,
    val expiryRisk: ExpiryRisk
)

data class OwnerUsage(
    val ownerId: Long,
    val ownerName: String,
    val occupiedSlots: Int,
    val occupancyPercentage: Int
)

data class ExpiryRisk(
    val expiredItems: List<RiskItem>,
    val expiringTodayItems: List<RiskItem>,
    val expiringWithinThreeDaysItems: List<RiskItem>,
    val expiringWithinSevenDaysItems: List<RiskItem>
) {
    val totalRiskItems: Int
        get() = expiredItems.size + expiringTodayItems.size +
            expiringWithinThreeDaysItems.size + expiringWithinSevenDaysItems.size
}

data class RiskItem(
    val itemId: Long,
    val itemName: String,
    val ownerId: Long,
    val ownerName: String,
    val expiryDate: LocalDate,
    val daysUntilExpiry: Long
)

object FridgeAnalysisCalculator {
    fun analyze(
        fridge: Fridge,
        contents: List<FridgeRecord>,
        items: List<Item>,
        owners: List<Owner>,
        today: LocalDate = LocalDate.now(ZoneOffset.UTC)
    ): FridgeAnalysis {
        val totalSlots = fridge.width * fridge.height * fridge.depth
        val itemById = items.associateBy { it.id }
        val ownerById = owners.associateBy { it.id }
        val placedItems = contents.mapNotNull { itemById[it.itemId] }
        val occupiedSlots = contents.size

        val ownerUsage = placedItems
            .groupingBy { it.ownerId }
            .eachCount()
            .map { (ownerId, count) ->
                OwnerUsage(
                    ownerId = ownerId,
                    ownerName = ownerById[ownerId]?.name ?: "Unknown owner",
                    occupiedSlots = count,
                    occupancyPercentage = percentage(count, totalSlots)
                )
            }
            .sortedWith(compareByDescending<OwnerUsage> { it.occupiedSlots }.thenBy { it.ownerName })

        val riskItems = placedItems
            .map { item ->
                val expiryDate = item.expiryDate.toLocalDate()
                RiskItem(
                    itemId = item.id,
                    itemName = item.name,
                    ownerId = item.ownerId,
                    ownerName = ownerById[item.ownerId]?.name ?: "Unknown owner",
                    expiryDate = expiryDate,
                    daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)
                )
            }
            .sortedWith(compareBy<RiskItem> { it.expiryDate }.thenBy { it.itemName })

        return FridgeAnalysis(
            fridgeId = fridge.id,
            fridgeName = fridge.name,
            totalSlots = totalSlots,
            occupiedSlots = occupiedSlots,
            freeSlots = totalSlots - occupiedSlots,
            occupancyPercentage = percentage(occupiedSlots, totalSlots),
            ownerUsage = ownerUsage,
            expiryRisk = ExpiryRisk(
                expiredItems = riskItems.filter { it.daysUntilExpiry < 0 },
                expiringTodayItems = riskItems.filter { it.daysUntilExpiry == 0L },
                expiringWithinThreeDaysItems = riskItems.filter { it.daysUntilExpiry in 1..3 },
                expiringWithinSevenDaysItems = riskItems.filter { it.daysUntilExpiry in 4..7 }
            )
        )
    }

    private fun percentage(value: Int, total: Int): Int {
        if (total == 0) return 0
        return ((value.toDouble() / total.toDouble()) * 100).toInt()
    }
}

private fun UtilDate.toLocalDate(): LocalDate {
    if (this is Date) return toLocalDate()
    return toInstant().atZone(ZoneOffset.UTC).toLocalDate()
}
