package io.initialcapacity.analyzer

import io.initialcapacity.workflow.Worker
import org.slf4j.LoggerFactory

class FridgeAnalysisWorker(
    private val analysisService: FridgeAnalysisService,
    override val name: String = "data-analyzer"
) : Worker<FridgeAnalysisTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(task: FridgeAnalysisTask) {
        logger.info("starting analysis for fridge {}", task.fridgeId)

        val analysis = analysisService.analyzeFridge(task.fridgeId)
        if (analysis == null) {
            logger.warn("fridge {} was not found while analyzing", task.fridgeId)
            return
        }

        logger.info(
            "completed analysis for fridge {}: {} occupied of {} slots, {} risk item(s)",
            analysis.fridgeId,
            analysis.occupiedSlots,
            analysis.totalSlots,
            analysis.expiryRisk.totalRiskItems
        )
    }
}
