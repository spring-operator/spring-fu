/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.fu.kofu.mongo

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoInitializer
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties
import org.springframework.context.support.GenericApplicationContext
import org.springframework.fu.kofu.AbstractDsl

/**
 * Kofu DSL for embedded MongoDB configuration.
 */
class EmbeddedMongoDsl(private val mongoProperties: MongoProperties, private val init: EmbeddedMongoDsl.() -> Unit) : AbstractDsl() {

	private val embeddedMongoProperties = EmbeddedMongoProperties()

	override fun register(context: GenericApplicationContext) {
		EmbeddedMongoInitializer(mongoProperties, embeddedMongoProperties).initialize(context)
	}

	/**
	 * Version of Mongo to use
	 */
	var version: String? = null
		set(value) {
			embeddedMongoProperties.version = value
		}
}

/**
 * Enable MongoDB embedded server.
 *
 * Require `de.flapdoodle.embed:de.flapdoodle.embed.mongo` dependency.
 *
 * @sample org.springframework.fu.kofu.samples.mongoEmbedded
 */
fun MongoDsl.embedded(dsl: EmbeddedMongoDsl.() -> Unit = {}) {
	embedded = true
	initializers.add(EmbeddedMongoDsl(properties, dsl))
}
