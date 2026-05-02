package io.initialcapacity.datamodel

data class Fridge(
    val id: Long,
    val name: String,
    val height: Int,
    val width: Int,
    val depth: Int,
    val contents: Map<Coordinate, Item> = emptyMap()
) {
    fun insertItem(item: Item, coordinate: Coordinate): Fridge {
        require(coordinate.x in 0 until width) { "x coordinate out of range" }
        require(coordinate.y in 0 until height) { "y coordinate out of range" }
        require(coordinate.z in 0 until depth) { "z coordinate out of range" }

        return copy(contents = contents + (coordinate to item))
    }

    fun itemAt(coordinate: Coordinate): Item? = contents[coordinate]
}
