package io.initialcapacity.analyzer

import io.initialcapacity.datamodel.DataGateway
import io.initialcapacity.workflow.WorkFinder
import org.slf4j.LoggerFactory

class FridgeAnalysisWorkFinder(private val dataGateway: DataGateway) : WorkFinder<FridgeAnalysisTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun findRequested(name: String): List<FridgeAnalysisTask> {
        logger.info("finding fridges to analyze for {}", name)
        return dataGateway.findAllFridges().map { FridgeAnalysisTask(it.id) }
    }

    override fun markCompleted(info: FridgeAnalysisTask) {
        logger.info("marked analysis complete for fridge {}", info.fridgeId)
    }
}
