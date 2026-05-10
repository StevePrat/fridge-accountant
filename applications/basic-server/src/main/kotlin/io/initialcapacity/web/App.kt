package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.analyzer.FridgeAnalysisService
import io.initialcapacity.database.createDatasource
import io.initialcapacity.datamodel.DataGateway
import io.initialcapacity.datamodel.FridgeRecord
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

private val logger = LoggerFactory.getLogger(object {}.javaClass.enclosingClass)
private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

fun Application.module() {
    logger.info("starting the app")

    val jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/fridge_dev"
    val dbUser = System.getenv("DB_USER") ?: "fridge_user"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "fridge_password"
    val gateway = DataGateway(createDatasource(jdbcUrl, dbUser, dbPassword))
    val analysisService = FridgeAnalysisService(gateway)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Routing) {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", homeModel(gateway, analysisService, call.request.queryParameters)))
        }

        get("/fridges/{id}") {
            val fridgeId = call.parameters["id"]?.toLongOrNull()
            if (fridgeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid fridge id")
                return@get
            }

            val fridge = gateway.findFridge(fridgeId)
            if (fridge == null) {
                call.respond(HttpStatusCode.NotFound, "Fridge not found")
                return@get
            }

            val contents = gateway.getFridgeContents(fridgeId)
            val items = gateway.findAllItems()
            val owners = gateway.findAllOwners()
            val itemMap = items.associateBy { it.id.toString() }
            val ownerMap = owners.associateBy { it.id.toString() }

            call.respond(FreeMarkerContent("fridge.ftl", mapOf(
                "fridge" to fridge,
                "contents" to contents,
                "items" to items,
                "itemMap" to itemMap,
                "ownerMap" to ownerMap,
                "analysis" to analysisService.analyzeFridge(fridge, contents, items, owners),
                "message" to call.request.queryParameters["message"],
                "error" to call.request.queryParameters["error"]
            )))
        }

        post("/fridges") {
            val params = call.receiveParameters()
            val name = params["name"]?.trim().orEmpty()
            val width = params["width"]?.toIntOrNull()
            val height = params["height"]?.toIntOrNull()
            val depth = params["depth"]?.toIntOrNull()

            call.respondRedirectWithResult("/", runCatching {
                if (name.isBlank() || width == null || height == null || depth == null) {
                    throw IllegalArgumentException("Please provide a name, width, height and depth.")
                }
                gateway.createFridge(name, width, height, depth)
                "Fridge '$name' created successfully."
            })
        }

        post("/owners") {
            val params = call.receiveParameters()
            val name = params["name"]?.trim().orEmpty()

            call.respondRedirectWithResult("/", runCatching {
                if (name.isBlank()) {
                    throw IllegalArgumentException("Please provide an owner name.")
                }
                gateway.createOwner(name)
                "Owner '$name' created successfully."
            })
        }

        post("/items") {
            val params = call.receiveParameters()
            val name = params["name"]?.trim().orEmpty()
            val expiry = params["expiry"]?.trim().orEmpty()
            val ownerId = params["ownerId"]?.toLongOrNull()

            call.respondRedirectWithResult("/", runCatching {
                if (name.isBlank() || expiry.isBlank() || ownerId == null) {
                    throw IllegalArgumentException("Please provide an item name, expiry date and owner.")
                }
                val expiryDate = dateFormat.parse(expiry)
                gateway.createItem(name, expiryDate, ownerId)
                "Item '$name' created successfully."
            })
        }

        post("/fridges/{id}/place-item") {
            val fridgeId = call.parameters["id"]?.toLongOrNull()
            val params = call.receiveParameters()
            val itemId = params["itemId"]?.toLongOrNull()
            val x = params["x"]?.toIntOrNull()
            val y = params["y"]?.toIntOrNull()
            val z = params["z"]?.toIntOrNull()

            if (fridgeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid fridge id")
                return@post
            }

            call.respondRedirectWithResult("/fridges/$fridgeId", runCatching {
                if (itemId == null || x == null || y == null || z == null) {
                    throw IllegalArgumentException("Please select an item and a valid x/y/z position.")
                }
                gateway.insertIntoFridge(FridgeRecord(0, fridgeId, itemId, x, y, z))
                "Item placed successfully."
            })
        }

        post("/fridge-records/{recordId}/delete") {
            val recordId = call.parameters["recordId"]?.toLongOrNull()

            if (recordId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid fridge record id")
                return@post
            }

            val record = gateway.findFridgeRecord(recordId)
            if (record == null) {
                call.respond(HttpStatusCode.NotFound, "Fridge record not found")
                return@post
            }

            val fridgeId = record.fridgeId
            call.respondRedirectWithResult("/fridges/$fridgeId", runCatching {
                gateway.deleteFromFridge(record)
                "Item removed successfully."
            })
        }

        post("/fridges/{id}/delete") {
            val fridgeId = call.parameters["id"]?.toLongOrNull()
            if (fridgeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid fridge id")
                return@post
            }
            call.respondRedirectWithResult("/", runCatching {
                gateway.deleteFridge(fridgeId)
                "Fridge deleted successfully."
            })
        }

        post("/items/{id}/delete") {
            val itemId = call.parameters["id"]?.toLongOrNull()
            if (itemId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid item id")
                return@post
            }
            call.respondRedirectWithResult("/", runCatching {
                gateway.deleteItem(itemId)
                "Item deleted successfully."
            })
        }

        post("/owners/{id}/delete") {
            val ownerId = call.parameters["id"]?.toLongOrNull()
            if (ownerId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid owner id")
                return@post
            }
            call.respondRedirectWithResult("/", runCatching {
                gateway.deleteOwner(ownerId)
                "Owner deleted successfully."
            })
        }

        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
}

private suspend fun ApplicationCall.respondRedirectWithResult(url: String, result: Result<String>) {
    val encoded = result.fold(
        onSuccess = { URLEncoder.encode(it, "UTF-8") },
        onFailure = { URLEncoder.encode(it.message ?: "Unknown error", "UTF-8") }
    )
    val target = if (result.isSuccess) "$url?message=$encoded" else "$url?error=$encoded"
    respondRedirect(target)
}

private fun homeModel(
    gateway: DataGateway,
    analysisService: FridgeAnalysisService,
    parameters: Parameters
): Map<String, Any?> {
    val fridges = gateway.findAllFridges()
    val owners = gateway.findAllOwners()
    val items = gateway.findAllItems()
    val analyses = analysisService.analyzeFridges(fridges, items, owners)

    return mapOf(
        "fridges" to fridges,
        "owners" to owners,
        "ownerMap" to owners.associateBy { it.id.toString() },
        "items" to items,
        "analyses" to analyses,
        "message" to parameters["message"],
        "error" to parameters["error"]
    )
}

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8888
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}
