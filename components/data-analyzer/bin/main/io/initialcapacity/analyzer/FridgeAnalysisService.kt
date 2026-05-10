package io.initialcapacity.analyzer

import io.initialcapacity.datamodel.DataGateway
import io.initialcapacity.datamodel.Fridge
import io.initialcapacity.datamodel.FridgeAnalysis
import io.initialcapacity.datamodel.FridgeAnalysisCalculator
import io.initialcapacity.datamodel.FridgeRecord
import io.initialcapacity.datamodel.Item
import io.initialcapacity.datamodel.Owner

class FridgeAnalysisService(private val dataGateway: DataGateway) {
    fun analyzeFridge(fridgeId: Long): FridgeAnalysis? {
        val fridge = dataGateway.findFridge(fridgeId) ?: return null
        return analyzeFridge(
            fridge = fridge,
            contents = dataGateway.getFridgeContents(fridgeId),
            items = dataGateway.findAllItems(),
            owners = dataGateway.findAllOwners()
        )
    }

    fun analyzeAllFridges(): List<FridgeAnalysis> {
        val fridges = dataGateway.findAllFridges()
        val items = dataGateway.findAllItems()
        val owners = dataGateway.findAllOwners()

        return analyzeFridges(fridges, items, owners)
    }

    fun analyzeFridges(
        fridges: List<Fridge>,
        items: List<Item>,
        owners: List<Owner>
    ): List<FridgeAnalysis> {
        return fridges.map { fridge ->
            analyzeFridge(
                fridge = fridge,
                contents = dataGateway.getFridgeContents(fridge.id),
                items = items,
                owners = owners
            )
        }
    }

    fun analyzeFridge(
        fridge: Fridge,
        contents: List<FridgeRecord>,
        items: List<Item>,
        owners: List<Owner>
    ): FridgeAnalysis = FridgeAnalysisCalculator.analyze(fridge, contents, items, owners)
}
