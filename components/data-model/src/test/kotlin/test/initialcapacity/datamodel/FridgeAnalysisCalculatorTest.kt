package test.initialcapacity.datamodel

import io.initialcapacity.datamodel.Fridge
import io.initialcapacity.datamodel.FridgeAnalysisCalculator
import io.initialcapacity.datamodel.FridgeRecord
import io.initialcapacity.datamodel.Item
import io.initialcapacity.datamodel.Owner
import org.junit.Test
import java.sql.Date
import java.time.LocalDate
import kotlin.test.assertEquals

class FridgeAnalysisCalculatorTest {
    @Test
    fun analyzesSpaceUsageOwnerUsageAndExpiryRisk() {
        val fridge = Fridge(id = 1, name = "Main fridge", height = 2, width = 2, depth = 2)
        val owners = listOf(
            Owner(id = 1, name = "Alice"),
            Owner(id = 2, name = "Bob")
        )
        val items = listOf(
            Item(id = 1, name = "Milk", expiryDate = Date.valueOf("2026-05-09"), ownerId = 1),
            Item(id = 2, name = "Eggs", expiryDate = Date.valueOf("2026-05-10"), ownerId = 1),
            Item(id = 3, name = "Juice", expiryDate = Date.valueOf("2026-05-13"), ownerId = 2),
            Item(id = 4, name = "Cheese", expiryDate = Date.valueOf("2026-05-17"), ownerId = 2)
        )
        val contents = listOf(
            FridgeRecord(id = 1, fridgeId = 1, itemId = 1, x = 0, y = 0, z = 0),
            FridgeRecord(id = 2, fridgeId = 1, itemId = 2, x = 1, y = 0, z = 0),
            FridgeRecord(id = 3, fridgeId = 1, itemId = 3, x = 0, y = 1, z = 0),
            FridgeRecord(id = 4, fridgeId = 1, itemId = 4, x = 1, y = 1, z = 0)
        )

        val analysis = FridgeAnalysisCalculator.analyze(
            fridge = fridge,
            contents = contents,
            items = items,
            owners = owners,
            today = LocalDate.of(2026, 5, 10)
        )

        assertEquals(8, analysis.totalSlots)
        assertEquals(4, analysis.occupiedSlots)
        assertEquals(4, analysis.freeSlots)
        assertEquals(50, analysis.occupancyPercentage)
        assertEquals(listOf("Alice", "Bob"), analysis.ownerUsage.map { it.ownerName })
        assertEquals(listOf(2, 2), analysis.ownerUsage.map { it.occupiedSlots })
        assertEquals(listOf("Milk"), analysis.expiryRisk.expiredItems.map { it.itemName })
        assertEquals(listOf("Eggs"), analysis.expiryRisk.expiringTodayItems.map { it.itemName })
        assertEquals(listOf("Juice"), analysis.expiryRisk.expiringWithinThreeDaysItems.map { it.itemName })
        assertEquals(listOf("Cheese"), analysis.expiryRisk.expiringWithinSevenDaysItems.map { it.itemName })
    }
}
