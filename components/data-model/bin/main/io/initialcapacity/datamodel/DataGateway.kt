package io.initialcapacity.datamodel

import io.initialcapacity.database.DatabaseTemplate
import io.initialcapacity.database.TransactionManager
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Date
import javax.sql.DataSource

class DataGateway(private val dataSource: DataSource) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val template = DatabaseTemplate(dataSource)
    private val transactionManager = TransactionManager(dataSource)

    fun findAllFridges(): List<Fridge> = template.query(
        sql = "SELECT id, name, height, width, depth FROM fridge",
        params = {},
        mapper = { rs -> Fridge(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("height"),
            rs.getInt("width"),
            rs.getInt("depth")
        ) }
    )

    fun findFridge(fridgeId: Long): Fridge? {
        return template.query(
            sql = "SELECT id, name, height, width, depth FROM fridge WHERE id = ?",
            mapper = { rs -> Fridge(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("height"),
                rs.getInt("width"),
                rs.getInt("depth")
            ) },
            params = { ps -> ps.setLong(1, fridgeId) }
        ).firstOrNull()
    }

    fun findAllItems(): List<Item> = template.query(
        sql = "SELECT id, name, expiry_date, owner_id FROM item",
        params = {},
        mapper = { rs -> Item(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("expiry_date"),
            rs.getLong("owner_id")
        ) }
    )

    fun findAllOwners(): List<Owner> = template.query(
        sql = "SELECT id, name FROM owner",
        params = {},
        mapper = { rs -> Owner(rs.getLong("id"), rs.getString("name")) }
    )

    fun getFridgeContents(fridgeId: Long): List<FridgeRecord> {
        return template.query(
            sql = "SELECT id, fridge_id, item_id, x, y, z FROM fridge_record WHERE fridge_id = ?",
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

    fun insertIntoFridge(fridgeRecord: FridgeRecord): FridgeRecord {
        return transactionManager.withTransaction { connection ->
            validateFridgeRecord(fridgeRecord, connection)
            
            val id = template.create(
                connection,
                sql = "INSERT INTO fridge_record (fridge_id, item_id, x, y, z) VALUES (?, ?, ?, ?, ?)",
                id = { it },
                fridgeRecord.fridgeId,
                fridgeRecord.itemId,
                fridgeRecord.x,
                fridgeRecord.y,
                fridgeRecord.z
            )
            fridgeRecord.copy(id = id)
        }
    }

    private fun validateFridgeRecord(fridgeRecord: FridgeRecord, connection: java.sql.Connection) {
        // Validate item exists
        validateItemExists(fridgeRecord.itemId, connection)
        
        // Get fridge dimensions
        val (width, height, depth) = getFridgeDimensions(fridgeRecord.fridgeId, connection)
        
        // Validate coordinates are within bounds
        if (fridgeRecord.x < 0 || fridgeRecord.x >= width) {
            throw IllegalArgumentException("X coordinate ${fridgeRecord.x} is out of bounds for fridge width $width")
        }
        if (fridgeRecord.y < 0 || fridgeRecord.y >= height) {
            throw IllegalArgumentException("Y coordinate ${fridgeRecord.y} is out of bounds for fridge height $height")
        }
        if (fridgeRecord.z < 0 || fridgeRecord.z >= depth) {
            throw IllegalArgumentException("Z coordinate ${fridgeRecord.z} is out of bounds for fridge depth $depth")
        }
        
        // Check if position is already occupied
        val existing = template.query(
            connection,
                sql = "SELECT id FROM fridge_record WHERE fridge_id = ? AND x = ? AND y = ? AND z = ?",
                ps.setLong(1, fridgeRecord.fridgeId)
                ps.setInt(2, fridgeRecord.x)
                ps.setInt(3, fridgeRecord.y)
                ps.setInt(4, fridgeRecord.z)
            }
        )
        
        if (existing.isNotEmpty()) {
            throw IllegalArgumentException("Position (${fridgeRecord.x}, ${fridgeRecord.y}, ${fridgeRecord.z}) is already occupied in fridge ${fridgeRecord.fridgeId}")
        }
    }

    private fun validateItemExists(itemId: Long, connection: java.sql.Connection) {
        val item = template.query(
            connection,
            sql = "SELECT id FROM item WHERE id = ?",
            mapper = { it.getLong("id") },
            params = { ps -> ps.setLong(1, itemId) }
        )
        
        if (item.isEmpty()) {
            throw IllegalArgumentException("Item with id $itemId not found")
        }
    }

    private fun validateOwnerExists(ownerId: Long, connection: java.sql.Connection) {
        val owner = template.query(
            connection,
            sql = "SELECT id FROM owner WHERE id = ?",
            mapper = { it.getLong("id") },
            params = { ps -> ps.setLong(1, ownerId) }
        )
        
        if (owner.isEmpty()) {
            throw IllegalArgumentException("Owner with id $ownerId not found")
        }
    }

    private fun getFridgeDimensions(fridgeId: Long, connection: java.sql.Connection): Triple<Int, Int, Int> {
        val fridgeQuery = template.query(
            connection,
            sql = "SELECT width, height, depth FROM fridge WHERE id = ?",
            mapper = { rs -> Triple(rs.getInt("width"), rs.getInt("height"), rs.getInt("depth")) },
            params = { ps -> ps.setLong(1, fridgeId) }
        )
        
        if (fridgeQuery.isEmpty()) {
            throw IllegalArgumentException("Fridge with id $fridgeId not found")
        }
        
        return fridgeQuery.first()
    }

    fun deleteFromFridge(fridgeRecord: FridgeRecord) {
        template.update(
            sql = "DELETE FROM fridge_record WHERE id = ?",
            fridgeRecord.id
        )
    }

    fun createFridge(name: String, width: Int, height: Int, depth: Int): Fridge {
        val id = template.create(
            sql = "INSERT INTO fridge (name, width, height, depth) VALUES (?, ?, ?, ?)",
            id = { it },
            name,
            width,
            height,
            depth
        )
        return Fridge(id, name, height, width, depth)
    }

    fun deleteFridge(fridgeId: Long) {
        return transactionManager.withTransaction { connection ->
            // Check if fridge has any items
            val items = template.query(
                connection,
                sql = "SELECT id FROM fridge_record WHERE fridge_id = ?",
                mapper = { it.getLong("id") },
                params = { ps -> ps.setLong(1, fridgeId) }
            )
            
            if (items.isNotEmpty()) {
                throw IllegalArgumentException("Cannot delete fridge $fridgeId - it contains ${items.size} item(s)")
            }
            
            // Delete the fridge
            template.update(
                connection,
                sql = "DELETE FROM fridge WHERE id = ?",
                fridgeId
            )
        }
    }

    fun createItem(name: String, expiryDate: Date, ownerId: Long): Item {
        return transactionManager.withTransaction { connection ->
            validateOwnerExists(ownerId, connection)
            
            val id = template.create(
                connection,
                sql = "INSERT INTO item (name, expiry_date, owner_id) VALUES (?, ?, ?)",
                id = { it },
                name,
                expiryDate,
                ownerId
            )
            Item(id, name, expiryDate, ownerId)
        }
    }

    fun deleteItem(itemId: Long) {
        return transactionManager.withTransaction { connection ->
            // Check if item is in any fridge
            val records = template.query(
                connection,
            sql = "SELECT id FROM fridge_record WHERE item_id = ?",
            )
            
            if (records.isNotEmpty()) {
                throw IllegalArgumentException("Cannot delete item $itemId - it is currently in ${records.size} fridge(s)")
            }
            
            // Delete the item
            template.update(
                connection,
                sql = "DELETE FROM item WHERE id = ?",
                itemId
            )
        }
    }

    fun createOwner(name: String): Owner {
        val id = template.create(
            sql = "INSERT INTO owner (name) VALUES (?)",
            id = { it },
            name
        )
        return Owner(id, name)
    }

    fun deleteOwner(ownerId: Long) {
        return transactionManager.withTransaction { connection ->
            // Check if owner has any items
            val items = template.query(
                connection,
                sql = "SELECT id FROM item WHERE owner_id = ?",
                mapper = { it.getLong("id") },
                params = { ps -> ps.setLong(1, ownerId) }
            )
            
            if (items.isNotEmpty()) {
                throw IllegalArgumentException("Cannot delete owner $ownerId - they own ${items.size} item(s)")
            }
            
            // Delete the owner
            template.update(
                connection,
                sql = "DELETE FROM owner WHERE id = ?",
                ownerId
            )
        }
    }
}