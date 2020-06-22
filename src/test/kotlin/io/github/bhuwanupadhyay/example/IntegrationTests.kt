package io.github.bhuwanupadhyay.example

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono
import java.util.function.Consumer

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(Alphanumeric::class)
@ActiveProfiles("test")
class IntegrationTests {
    @LocalServerPort
    private val port = 0
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repositories: List<CrudRepository<*, *>>

    @BeforeEach
    fun setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @AfterEach
    fun tearDown() {
        repositories.forEach(Consumer { obj: CrudRepository<*, *> -> obj.deleteAll() })
    }

    @Test
    fun `return 201 if order created successfully`() {
        orderCreate(OrderRequest("item", 10))
                .expectStatus()
                .isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.item").isEqualTo("item")
                .jsonPath("$.quantity").isEqualTo(10)
    }

    @Test
    fun `return 400 if order not created successfully`() {
        orderCreate(null).expectStatus().isBadRequest
    }

    @Test
    fun `return 200 if order get by id successfully`() {
        val body = orderCreated(OrderRequest("item", 10))
        body.jsonPath("$.id").value { id: Long ->
            client
                    .get()
                    .uri("/orders/$id").exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$.item").isEqualTo("item")
                    .jsonPath("$.quantity").isEqualTo(10)
        }
    }

    @Test
    fun `return 404 if order id is not found`() {
        client.get().uri("/orders/10000000000").exchange().expectStatus().isNotFound
    }

    @Test
    fun `return 400 if order id is not positive number`() {
        client.get().uri("/orders/   ").exchange().expectStatus().isBadRequest
        client.get().uri("/orders/0").exchange().expectStatus().isBadRequest
        client.get().uri("/orders/-1").exchange().expectStatus().isBadRequest
        client.get().uri("/orders/abc").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `return 200 if order update successfully`() {
        val body = orderCreated(OrderRequest("item", 10))
        body.jsonPath("$.id").value { id: Long ->
            client
                    .put()
                    .uri("/orders/$id").bodyValue(OrderRequest("item-changed", 20)).exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$.item").isEqualTo("item-changed")
                    .jsonPath("$.quantity").isEqualTo(20)
        }
    }

    @Test
    fun `return 400 if not order update successfully`() {
        val body = orderCreated(OrderRequest("item", 10))
        body.jsonPath("$.id").value { id: Long ->
            client
                    .put()
                    .uri("/orders/$id").body(Mono.justOrEmpty(null), OrderRequest::class.java)
                    .exchange().expectStatus().isBadRequest
        }
    }

    @Test
    fun `return 404 if try update wrong order`() {
        client
                .put()
                .uri("/orders/10000000000").bodyValue(OrderRequest("item-changed", 20))
                .exchange().expectStatus().isNotFound
    }

    @Test
    fun `return 200 if order list successfully`() {
        `return 201 if order created successfully`()
        `return 201 if order created successfully`()
        client
                .get()
                .uri("/orders").exchange().expectStatus().isOk.expectBody()
                .jsonPath("$.size()").isEqualTo(2)
    }

    private fun orderCreated(request: OrderRequest?): BodyContentSpec {
        return orderCreate(request)
                .expectStatus()
                .isCreated
                .expectBody()
    }

    private fun orderCreate(request: OrderRequest?): WebTestClient.ResponseSpec {
        return client
                .post()
                .uri("/orders")
                .body(Mono.justOrEmpty(request), OrderRequest::class.java)
                .exchange()

    }
}
