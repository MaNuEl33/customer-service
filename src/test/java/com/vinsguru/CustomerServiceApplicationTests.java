package com.vinsguru;

import com.vinsguru.client.MovieClient;
import com.vinsguru.domain.Genre;
import com.vinsguru.dto.CustomerDto;
import com.vinsguru.dto.GenreUpdateRequest;
import com.vinsguru.dto.MovieDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Import(TestcontainersConfiguration.class)
@MockitoBean(types = {RestClient.class, MovieClient.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerServiceApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(CustomerServiceApplicationTests.class);

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MovieClient movieClient;

	@Test
	void health() {
		final var responseEntity = this.restTemplate.getForEntity("/actuator/health", Object.class);
		Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
	}

	@Test
	void customerWithMovies() {
		Mockito.when(this.movieClient.getMovies(Mockito.any(Genre.class))).thenReturn(List.of(
				new MovieDto(1, "movie-1", 1990, Genre.ACTION),
				new MovieDto(2, "movie-2", 1991, Genre.ACTION)
		));

		final var responseEntity = this.restTemplate.getForEntity("/api/customers/1", CustomerDto.class);
		Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

		final var customerDto = responseEntity.getBody();
		Assertions.assertNotNull(customerDto);
		Assertions.assertEquals("sam", customerDto.name());
		Assertions.assertEquals(2, customerDto.recommendedMovies().size());
	}

	@Test
	void customerNotFound() {
		final var responseEntity = this.restTemplate.getForEntity("/api/customers/10", ProblemDetail.class);
		Assertions.assertTrue(responseEntity.getStatusCode().is4xxClientError());

		final var problemDetail = responseEntity.getBody();
		Assertions.assertNotNull(problemDetail);
		log.info("Problem detail: {}", problemDetail);

		Assertions.assertEquals("Customer Not Found", problemDetail.getTitle());
	}

	@Test
	void updateGenre() {
		final var genreUpdateRequest = new GenreUpdateRequest(Genre.DRAMA);
		final var requestEntity = new RequestEntity<>(genreUpdateRequest, HttpMethod.PATCH, URI.create("/api/customers/1/genre"));
		final var responseEntity = this.restTemplate.exchange(requestEntity, Void.class);

		Assertions.assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}
}
