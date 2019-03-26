package org.springframework.fu.jafu.web;

import static org.springframework.fu.jafu.ApplicationDsl.application;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;

public class MustacheDslTests {

	@Test
	void createAndRequestAMustacheView() {
		var router = RouterFunctions
				.route()
				.GET("/view", request -> ok().render("template", Collections.singletonMap("name", "world")))
				.build();
		var app = application(a -> a.enable(WebFluxServerDsl.class, s -> s.mustache().importRouter(router)));

		var context = app.run();
		var client = WebTestClient.bindToServer().baseUrl("https://0.0.0.0:8080").build();
		client.get().uri("/view").exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(String.class)
				.isEqualTo("Hello world!");
		context.close();
	}

}
