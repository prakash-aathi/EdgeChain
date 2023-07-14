package com.edgechain.openai;

import com.edgechain.lib.embeddings.WordEmbeddings;
import com.edgechain.lib.endpoint.impl.OpenAiEndpoint;
import com.edgechain.lib.openai.response.ChatCompletionResponse;
import com.edgechain.lib.rxjava.retry.impl.ExponentialDelay;
;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.TimeUnit;

import static com.edgechain.lib.constants.EndpointConstants.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenAiClientTest {

  @LocalServerPort int randomServerPort;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @BeforeEach
  public void setup() {
    System.setProperty("server.port", "" + randomServerPort);
  }

  @ParameterizedTest
  @CsvSource({
    "Write 10 unique sentences on Java Language",
    "Can you explain Ant Bee Colony Optimization Algorithm?"
  })
  public void testOpenAiEndpoint_ChatCompletionShouldAssertNoErrors(String prompt)
      throws InterruptedException {

    // Step 1 : Create OpenAi Endpoint
    OpenAiEndpoint endpoint =
        new OpenAiEndpoint(
            OPENAI_CHAT_COMPLETION_API,
            "", // apiKey
            "", // orgId
            "gpt-3.5-turbo",
            "user",
            0.7,
            false,
            new ExponentialDelay(3, 3, 2, TimeUnit.SECONDS));

    TestObserver<ChatCompletionResponse> test = endpoint.getChatCompletion(prompt).test();

    // Step 4: To act & assert
    test.await();

    logger.info(test.values().toString());

    // Assert
    test.assertNoErrors();
  }

  @ParameterizedTest
  @CsvSource({
    "Write 10 unique sentences on Java Language",
    "Can you explain Ant Bee Colony Optimization Algorithm?"
  })
  @DisplayName("Test OpenAI ChatCompletion Stream")
  public void testOpenAiEndpoint_ChatCompletionStreamResponseShouldAssertNoErrors(String prompt)
      throws InterruptedException {

    // Step 1 : Create OpenAi Endpoint
    OpenAiEndpoint endpoint =
        new OpenAiEndpoint(
            OPENAI_CHAT_COMPLETION_API,
            "", // apiKey
            "", // orgId
            "gpt-3.5-turbo",
            "user",
            0.7,
            true,
            new ExponentialDelay(3, 3, 2, TimeUnit.SECONDS));

    TestObserver<ChatCompletionResponse> test = endpoint.getChatCompletion(prompt).test();

    // Step 4: To act & assert
    test.await();

    logger.info(test.values().toString());

    // Assert
    test.assertNoErrors();
  }

  @Test
  @DisplayName("Test OpenAI Embeddings")
  public void testOpenAiEndpoint_EmbeddingsShouldAssertNoErrors() throws InterruptedException {

    String input = "Hey, we are building LLMs using Spring and Java";

    // Step 1 : Create OpenAi Endpoint
    OpenAiEndpoint endpoint =
        new OpenAiEndpoint(
            OPENAI_EMBEDDINGS_API,
            "", // apiKey
            "", // orgId
            "text-embedding-ada-002", // model
            null,
            null,
            null,
            new ExponentialDelay(3, 3, 2, TimeUnit.SECONDS));

    TestObserver<WordEmbeddings> test = endpoint.getEmbeddings(input).test();

    // Step 4: To act & assert
    test.await();

    logger.info(test.values().toString());

    // Assert
    test.assertNoErrors();
  }
}