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

package org.springframework.web.function

import org.springframework.core.ResolvableType.forClass
import org.springframework.core.ResolvableType.forClassWithGenerics
import org.springframework.http.MediaType
import org.springframework.http.codec.CoHttpMessageReader
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.coroutines.CoServerHttpRequest
import org.springframework.util.MultiValueMap

private val FORM_MAP_TYPE = forClassWithGenerics(MultiValueMap::class.java, String::class.java, String::class.java)

private val MULTIPART_MAP_TYPE = forClassWithGenerics(
	MultiValueMap::class.java, String::class.java, Part::class.java
)

private val PART_TYPE = forClass(Part::class.java)


fun toFormData(): CoBodyExtractor<MultiValueMap<String, String>?, CoServerHttpRequest> =
	object : CoBodyExtractor<MultiValueMap<String, String>?, CoServerHttpRequest> {
		override suspend fun extract(
				inputMessage: CoServerHttpRequest,
				context: CoBodyExtractor.Context
		): MultiValueMap<String, String>? {
			val messageReader = messageReader<MultiValueMap<String, String>>(
				FORM_MAP_TYPE,
				MediaType.APPLICATION_FORM_URLENCODED,
				context
			)
			return context.serverResponse()
				?.let {
					val readSingle = messageReader.readSingle(
						actualType = FORM_MAP_TYPE,
						elementType = FORM_MAP_TYPE,
						request = inputMessage,
						response = it,
						hints = context.hints()
					)
					readSingle
				}
					?: messageReader.readSingle(FORM_MAP_TYPE, inputMessage, context.hints())
		}
	}

@Suppress("UNCHECKED_CAST")
private fun <T> messageReader(
	elementType: org.springframework.core.ResolvableType,
	mediaType: MediaType, context: CoBodyExtractor.Context
): CoHttpMessageReader<T> =
	context.messageReaders().invoke()
		.filter { messageReader -> messageReader.canRead(elementType, mediaType) }
		.firstOrNull()
		?.let { it as CoHttpMessageReader<T> }
			?: throw
			IllegalStateException(
				"""Could not find HttpMessageReader that supports "$mediaType" and "$elementType""""
			)

