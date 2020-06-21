package com.fnovellon.klab

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = true) {
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = Json(configuration = JsonConfiguration.Stable)
        )
    }
    install(StatusPages) {
        exception<DomainException> { cause ->
            call.respond(cause.httpStatusCode, DomainExceptionComposition(listOf(cause)))
        }
        exception<DomainExceptionComposition> { cause ->
            call.respond(HttpStatusCode.MultiStatus, cause)
        }
        status(HttpStatusCode.OK) {
            log.debug("OK SEND")
            //call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
        }
    }

    routing {
        get("d") {
            throw DomainException.Wtf
        }
        get("t") {
            throw DomainException.Wtf + DomainException.CPasMonBoulo
        }
        get("ok") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

val DomainException.Companion.Wtf: DomainException
    get() = DomainException(201, "WtfDomainException", "WTF comment t'es arrivé la ?")
val DomainException.Companion.CPasMonBoulo: DomainException
    get() = DomainException(200, "CPasMonBoulo", "Tu me prends pour qui ?", HttpStatusCode.SeeOther)

@Serializable
class DomainException(
    val code: Int,
    val name: String,
    val description: String,
    @Transient val httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
) : Throwable() {

    operator fun plus(domainException: DomainException): DomainExceptionComposition =
        DomainExceptionComposition(errors = listOf(this, domainException))

    companion object

}

@Serializable
class DomainExceptionComposition(
    val errors: List<DomainException>
) : Throwable() {

    operator fun plus(domainException: DomainException): DomainExceptionComposition =
        DomainExceptionComposition(errors = errors + domainException)

}
