package com.mantecasl.accommodationapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class AccommodationappApplicationTest {

	@Test
	void contextLoads() {
		// Carga completa del contexto Spring
	}

	@Test
	void main_executes_without_exceptions() {
		assertDoesNotThrow(() -> AccommodationappApplication.main(new String[] {}));
	}
}
