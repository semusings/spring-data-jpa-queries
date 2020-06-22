package io.github.bhuwanupadhyay.example

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class OrderEntity(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long?, var item: String, var quantity: Int)

interface OrderRepository : CrudRepository<OrderEntity, Long>

class DomainException(override val message: String, val ex: Throwable?) : RuntimeException(message, ex)

data class OrderRequest(var item: String, var quantity: Int)

@Component
class OrderHandler(private val repository: OrderRepository) {

    fun findAll(req: ServerRequest) = ok().body(BodyInserters.fromValue(repository.findAll()))

    fun findOne(req: ServerRequest): Mono<ServerResponse> {
        return repository.findById(evalId(req)).map { ok().bodyValue(it) }.orElseGet { notFound().build() }
    }

    fun save(req: ServerRequest): Mono<ServerResponse> {
        val payload = req.body(BodyExtractors.toMono(OrderRequest::class.java))
        return payload.flatMap { status(HttpStatus.CREATED).body(BodyInserters.fromValue(repository.save(OrderEntity(null, it.item, it.quantity)))) }.switchIfEmpty(badRequest().build())
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        val id = evalId(req)
        val exists = Optional.ofNullable(repository.existsById(id)).filter { it }.map { Mono.just(it) }.orElseGet { Mono.empty() }
        return exists
                .flatMap {
                    if (it) {
                        val payload = req.body(BodyExtractors.toMono(OrderRequest::class.java))
                        payload.flatMap { request -> ok().bodyValue(repository.save(OrderEntity(id, request.item, request.quantity))) }.switchIfEmpty(badRequest().build())
                    } else {
                        notFound().build()
                    }
                }.switchIfEmpty(notFound().build())
    }

    private fun evalId(req: ServerRequest): Long {
        try {
            return Optional.ofNullable(req.pathVariable("id").toLong()).filter { it > 0 }.orElseThrow { throw DomainException(message = "Id should be positive number.", ex = null) }
        } catch (ex: NumberFormatException) {
            throw DomainException(message = "Id should be positive number.", ex = ex)
        }
    }
}

@Configuration
class OrderRoutes(private val handler: OrderHandler) {

    private val log: Logger = LoggerFactory.getLogger(OrderRoutes::class.java)

    @Bean
    fun router() = router {
        accept(APPLICATION_JSON).nest {
            POST("/orders", handler::save)
            GET("/orders", handler::findAll)
            GET("/orders/{id}", handler::findOne)
            PUT("/orders/{id}", handler::update)
        }
    }.filter { request, next ->
        try {
            next.handle(request)
        } catch (ex: Exception) {
            log.error("Error on rest interface.", ex)
            when (ex) {
                is DomainException -> badRequest().build()
                else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }
}
