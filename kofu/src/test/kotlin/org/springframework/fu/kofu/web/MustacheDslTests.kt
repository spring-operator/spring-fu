/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.fu.kofu.web

import org.junit.jupiter.api.Test
import org.springframework.fu.kofu.webApplication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

/**
 * @author Sebastien Deleuze
 */
class MustacheDslTests {

	@Test
	fun `Create and request a Mustache view`() {
		val app = webApplication {
			server {
				mustache()
				router {
					GET("/view") { ok().render("template", mapOf("name" to "world")) }
				}
			}
		}
		val context = app.run()
		val client = WebTestClient.bindToServer().baseUrl("https://0.0.0.0:8080").build()
		client.get().uri("/view").exchange()
			.expectStatus().is2xxSuccessful
			.expectBody<String>()
			.isEqualTo("Hello world!")
		context.close()
	}

}