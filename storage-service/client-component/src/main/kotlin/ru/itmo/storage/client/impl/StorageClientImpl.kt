package ru.itmo.storage.client.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.itmo.storage.client.StorageClient
import ru.itmo.storage.client.exception.BadServerResponseException
import ru.itmo.storage.client.exception.NoServerResponseException
import ru.itmo.storage.client.response.ErrorResponse

@Component
class StorageClientImpl(
    private val webClient: WebClient
) : StorageClient {

    override fun get(key: String): String {
        val requestSpec = webClient.get()
            .uri { uriBuilder -> uriBuilder.path("/keys/{key}").build(key) }

        return performRequest(requestSpec)
            .bodyToMono(String::class.java)
            .block() ?: throw NoServerResponseException()
    }

    override fun set(key: String, value: String) {
        val requestSpec = webClient.put()
            .uri { uriBuilder -> uriBuilder.path("/keys/{key}").queryParam("value", value).build(key) }

        performRequest(requestSpec)
            .toBodilessEntity()
            .block() ?: throw NoServerResponseException()
    }

    private fun <S : WebClient.RequestHeadersSpec<S>?> performRequest(
        requestBodySpec: WebClient.RequestHeadersSpec<S>
    ) = requestBodySpec
        .retrieve()
        .onStatus({ httpStatus -> httpStatus == HttpStatus.NOT_FOUND })
            { response -> errorResponseToMono(response) }
        .onStatus({ httpStatus -> httpStatus == HttpStatus.BAD_REQUEST })
            { response -> errorResponseToMono(response) }
        .onStatus({ httpStatus -> httpStatus == HttpStatus.UNPROCESSABLE_ENTITY })
            { response -> errorResponseToMono(response) }
        .onStatus({ httpStatus -> httpStatus == HttpStatus.SERVICE_UNAVAILABLE })
            { response -> errorResponseToMono(response) }
        .onStatus({ httpStatus -> httpStatus == HttpStatus.INTERNAL_SERVER_ERROR })
            { response -> errorResponseToMono(response) }

    private fun errorResponseToMono(response: ClientResponse): Mono<BadServerResponseException> =
        response.bodyToMono(ErrorResponse::class.java).map { body ->
            BadServerResponseException(
                body.message
            )
        }
}