/*
 * Copyright 2020-2021 the original author or authors.
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
package example.springdata.elasticsearch.conference;

import static org.assertj.core.api.Assertions.*;

import example.springdata.elasticsearch.util.EnabledOnElasticsearch;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

/**
 * Test case to show Spring Data Elasticsearch functionality.
 *
 * @author Christoph Strobl
 * @author Prakhar Gupta
 */
@EnabledOnElasticsearch
@SpringBootTest(classes = ApplicationConfiguration.class)
class ReactiveElasticsearchOperationsTest {

	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	ReactiveElasticsearchOperations operations;

	@Test
	void textSearch() {

		String expectedDate = "2014-10-29";
		String expectedWord = "java";
		CriteriaQuery query = new CriteriaQuery(
				new Criteria("keywords").contains(expectedWord).and("date").greaterThanEqual(expectedDate));

		operations.search(query, Conference.class) //
				.as(StepVerifier::create) //
				.consumeNextWith(it -> verify(it, expectedWord, expectedDate)) //
				.consumeNextWith(it -> verify(it, expectedWord, expectedDate)) //
				.consumeNextWith(it -> verify(it, expectedWord, expectedDate)) //
				.verifyComplete();
	}

	private void verify(SearchHit<Conference> hit, String expectedWord, String expectedDate) {

		assertThat(hit.getContent().getKeywords()).contains(expectedWord);
		try {
			assertThat(format.parse(hit.getContent().getDate())).isAfter(format.parse(expectedDate));
		} catch (ParseException e) {
			fail("o_O", e);
		}
	}
}
