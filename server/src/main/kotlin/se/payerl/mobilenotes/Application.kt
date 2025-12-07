package se.payerl.mobilenotes

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import se.payerl.mobilenotes.database.DatabaseConfig
import se.payerl.mobilenotes.routes.noteAdvancedRoutes
import se.payerl.mobilenotes.routes.noteRoutes
import se.payerl.mobilenotes.service.NoteReferenceService
import se.payerl.mobilenotes.service.NoteService

fun main() {
    // Initiera databasen
    DatabaseConfig.init()

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Installera Content Negotiation för JSON-serialisering
    install(ContentNegotiation) {
        json()
    }

    val noteService = NoteService()
    val noteReferenceService = NoteReferenceService(noteService)

    routing {
        // Swagger UI endpoint - öppnas på http://localhost:8080/swagger
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "4.15.5"
        }

        // OpenAPI specification endpoint
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // Grundläggande CRUD endpoints (GET, POST, PUT, DELETE)
        noteRoutes(noteService)

        // Avancerade operationer: kopiera och referera listor
        noteAdvancedRoutes(noteService, noteReferenceService)

        get("/posts") {
            call.respondText("List of posts will be here.")
        }
    }
}
