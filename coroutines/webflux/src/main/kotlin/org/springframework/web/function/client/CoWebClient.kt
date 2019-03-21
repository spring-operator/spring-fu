/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.function.client

import kotlinx.coroutines.reactive.awaitFirstOrDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.nio.charset.Charset
import java.time.ZonedDateTime

interface CoWebClient {
	fun get(): RequestHeadersUriSpec<*>

	fun head(): RequestHeadersUriSpec<*>

	fun post(): RequestBodyUriSpec

	fun put(): RequestBodyUriSpec

	fun patch(): RequestBodyUriSpec

	fun delete(): RequestHeadersUriSpec<*>

	fun options(): RequestHeadersUriSpec<*>

	fun method(method: HttpMethod): RequestBodyUriSpec

	interface RequestBodyUriSpec: RequestBodySpec, RequestHeadersUriSpec<RequestBodySpec>

	interface RequestBodySpec: RequestHeadersSpec<RequestBodySpec>

	interface RequestHeadersUriSpec<T: RequestHeadersSpec<T>>: UriSpec<T>, RequestHeadersSpec<T>

	interface UriSpec<T: RequestHeadersSpec<T>> {
		fun uri(uri: String, vararg uriVariables: Any): T

		fun uri(uri: String, uriVariables: Map<String, *>): T

		fun uri(uri: URI): T
	}

	interface RequestHeadersSpec<T: RequestHeadersSpec<T>> {
		fun accept(vararg acceptableMediaTypes: MediaType): T

		fun acceptCharset(vararg acceptableCharsets: Charset): T

		fun cookie(name: String, value: String): T

		fun cookies(cookiesConsumer: (MultiValueMap<String, String>) -> Unit): T

		fun ifModifiedSince(ifModifiedSince: ZonedDateTime): T

		fun ifNoneMatch(vararg ifNoneMatches: String): T

		fun header(headerName: String, vararg headerValues: String): T

		fun headers(headersConsumer: (HttpHeaders) -> Unit): T

		fun attribute(name: String, value: Any): T

		fun attributes(attributesConsumer: (Map<String, Any>) -> Unit): T

		suspend fun retrieve(): CoroutineResponseSpec

		suspend fun exchange(): CoClientResponse?
	}

	interface CoroutineResponseSpec {
		suspend fun <T> body(clazz: Class<T>): T?
	}

	fun WebClient.asCoroutines() = DefaultCoWebClient(this)
}

suspend inline fun <reified T : Any> CoWebClient.CoroutineResponseSpec.body(): T? = body(T::class.java)

open class DefaultCoWebClient(private val client: WebClient) : CoWebClient {

	override fun get(): CoWebClient.RequestHeadersUriSpec<*> = request { client.get() }

	override fun head(): CoWebClient.RequestHeadersUriSpec<*> = request { client.head() }

	override fun post(): CoWebClient.RequestBodyUriSpec = request { client.post() }

	override fun put(): CoWebClient.RequestBodyUriSpec = request { client.put() }

	override fun patch(): CoWebClient.RequestBodyUriSpec = request { client.patch() }

	override fun delete(): CoWebClient.RequestHeadersUriSpec<*> = request { client.delete() }

	override fun options(): CoWebClient.RequestHeadersUriSpec<*> = request { client.options() }

	override fun method(method: HttpMethod): CoWebClient.RequestBodyUriSpec = request { client.method(method) }

	private fun request(f: () -> WebClient.RequestHeadersUriSpec<*>): CoWebClient.RequestBodyUriSpec =
		DefaultRequestBodyUriSpec(f.invoke() as WebClient.RequestBodyUriSpec)
}

private fun WebClient.ResponseSpec.asCoroutines(): CoWebClient.CoroutineResponseSpec =
		DefaultCoResponseSpec(this)

open class DefaultCoResponseSpec(
	private val spec: WebClient.ResponseSpec
): CoWebClient.CoroutineResponseSpec {
	override suspend fun <T> body(clazz: Class<T>): T? =
			spec.bodyToMono(clazz).awaitFirstOrDefault(null)
}

open class DefaultRequestBodyUriSpec(
	private val spec: WebClient.RequestBodyUriSpec
): CoWebClient.RequestBodyUriSpec {
	override fun uri(uri: String, vararg uriVariables: Any): CoWebClient.RequestBodySpec = apply {
		spec.uri(uri, *uriVariables)
	}

	override fun uri(uri: String, uriVariables: Map<String, *>): CoWebClient.RequestBodySpec = apply {
		spec.uri(uri, uriVariables)
	}

	override fun uri(uri: URI): CoWebClient.RequestBodySpec = apply {
		spec.uri(uri)
	}

	override fun accept(vararg acceptableMediaTypes: MediaType): CoWebClient.RequestBodySpec = apply {
		spec.accept(*acceptableMediaTypes)
	}

	override fun acceptCharset(vararg acceptableCharsets: Charset): CoWebClient.RequestBodySpec = apply {
		spec.acceptCharset(*acceptableCharsets)
	}

	override fun cookie(name: String, value: String): CoWebClient.RequestBodySpec = apply {
		spec.cookie(name, value)
	}

	override fun cookies(cookiesConsumer: (MultiValueMap<String, String>) -> Unit): CoWebClient.RequestBodySpec = apply {
		spec.cookies(cookiesConsumer)
	}

	override fun ifModifiedSince(ifModifiedSince: ZonedDateTime): CoWebClient.RequestBodySpec = apply {
		spec.ifModifiedSince(ifModifiedSince)
	}

	override fun ifNoneMatch(vararg ifNoneMatches: String): CoWebClient.RequestBodySpec = apply {
		spec.ifNoneMatch(*ifNoneMatches)
	}

	override fun header(headerName: String, vararg headerValues: String): CoWebClient.RequestBodySpec = apply {
		spec.header(headerName, *headerValues)
	}

	override fun headers(headersConsumer: (HttpHeaders) -> Unit): CoWebClient.RequestBodySpec = apply {
		spec.headers(headersConsumer)
	}

	override fun attribute(name: String, value: Any): CoWebClient.RequestBodySpec = apply {
		spec.attribute(name, value)
	}

	override fun attributes(attributesConsumer: (Map<String, Any>) -> Unit): CoWebClient.RequestBodySpec = apply {
		spec.attributes(attributesConsumer)
	}

	override suspend fun exchange(): CoClientResponse? = spec.exchange().awaitFirstOrDefault(null)?.let {
		DefaultCoClientResponse(it)
	}

	override suspend fun retrieve(): CoWebClient.CoroutineResponseSpec =
		spec.retrieve().asCoroutines()
}