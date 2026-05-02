package io.initialcapacity.datamodel

import io.initialcapacity.database.DatabaseTemplate
import io.initialcapacity.database.TransactionManager
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

class DataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)

    fun findByFridgeId(fridgeId: Long): List<FridgeRecord> {
        return template.query(
            sql = "SELECT id, fridge_id, item_id, x, y, z FROM fridge_records WHERE fridge_id = ?",
            mapper = { rs: ResultSet -> FridgeRecord(
                rs.getLong("id"),
                rs.getLong("fridge_id"),
                rs.getLong("item_id"),
                rs.getInt("x"),
                rs.getInt("y"),
                rs.getInt("z")
            )},
            params = { ps: PreparedStatement -> ps.setLong(1, fridgeId) }
        )
    }
}