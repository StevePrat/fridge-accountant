package io.initialcapacity.datamodel

import java.util.Date

data class Item(val id: Long, val name: String, val expiryDate: Date, val ownerId: Long)
