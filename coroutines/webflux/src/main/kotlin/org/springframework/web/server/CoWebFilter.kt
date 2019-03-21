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

package org.springframework.web.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono

interface CoWebFilter : WebFilter {

	@Suppress("UNCHECKED_CAST")
	override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> = GlobalScope.mono(Dispatchers.Unconfined) {
		filter(CoServerWebExchange(exchange), CoWebFilterChain(chain))
	} as Mono<Void>

	suspend fun filter(exchange: CoServerWebExchange, chain: CoWebFilterChain)
}