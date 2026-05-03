package io.initialcapacity.datamodel

data class Fridge(
    val id: Long,
    val name: String,
    val height: Int,
    val width: Int,
    val depth: Int,
    val contents: Map<Coordinate, Long> = emptyMap()
) {
    fun insertItem(itemId: Long, coordinate: Coordinate): Fridge {
        require(coordinate.x in 0 until width) { "x coordinate out of range" }
        require(coordinate.y in 0 until height) { "y coordinate out of range" }
        require(coordinate.z in 0 until depth) { "z coordinate out of range" }

        return copy(contents = contents + (coordinate to itemId))
    }

    fun removeItem(coordinate: Coordinate): Fridge {
        require(coordinate.x in 0 until width) { "x coordinate out of range" }
        require(coordinate.y in 0 until height) { "y coordinate out of range" }
        require(coordinate.z in 0 until depth) { "z coordinate out of range" }

        return copy(contents = contents - coordinate)
    }

    fun toRecords(): List<FridgeRecord> = contents.map { (coordinate, itemId) ->
        FridgeRecord(
            id = 0L,
            fridgeId = id,
            itemId = itemId,
            x = coordinate.x,
            y = coordinate.y,
            z = coordinate.z
        )
    }

    fun toRecordArray(): Array<FridgeRecord> = toRecords().toTypedArray()

    fun populateFromRecords(records: Collection<FridgeRecord>): Fridge {
        val contents = records
            .asSequence()
            .filter { it.fridgeId == id }
            .map { record ->
                require(record.x in 0 until width) { "x coordinate out of range" }
                require(record.y in 0 until height) { "y coordinate out of range" }
                require(record.z in 0 until depth) { "z coordinate out of range" }
                Coordinate(record.x, record.y, record.z) to record.itemId
            }
            .toMap()

        return copy(contents = contents)
    }

    fun populateFromRecords(records: Array<FridgeRecord>): Fridge = populateFromRecords(records.asList())
}
