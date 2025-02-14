package com.edgechain.lib.context.client.impl;

import com.edgechain.lib.context.client.HistoryContextClient;
import com.edgechain.lib.context.client.repositories.PostgreSQLHistoryContextRepository;
import com.edgechain.lib.context.domain.HistoryContext;
import com.edgechain.lib.endpoint.impl.PostgreSQLHistoryContextEndpoint;
import com.edgechain.lib.rxjava.transformer.observable.EdgeChain;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PostgreSQLHistoryContextClient
    implements HistoryContextClient<PostgreSQLHistoryContextEndpoint> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired private PostgreSQLHistoryContextRepository historyContextRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final String PREFIX = "historycontext:";

  @Override
  public EdgeChain<HistoryContext> create(String id, PostgreSQLHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {

                if (Objects.isNull(id) || id.isEmpty())
                  throw new RuntimeException("Postgres history_context id cannot be empty or null");

                this.createTable(); // Create Table IF NOT EXISTS;

                HistoryContext context = new HistoryContext(PREFIX + id, "", LocalDateTime.now());
                emitter.onNext(historyContextRepository.save(context));
                emitter.onComplete();

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<HistoryContext> put(
      String id, String response, PostgreSQLHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {

                HistoryContext historyContext = this.get(id, null).get();
                String input = response.replaceAll("'", "");
                historyContext.setResponse(input);

                HistoryContext returnValue = this.historyContextRepository.save(historyContext);
                logger.info(String.format("%s is updated", id));

                emitter.onNext(returnValue);
                emitter.onComplete();

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<HistoryContext> get(String id, PostgreSQLHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {
                emitter.onNext(
                    this.historyContextRepository
                        .findById(id)
                        .orElseThrow(
                            () ->
                                new RuntimeException(
                                    "PostgreSQL history_context id isn't found.")));
                emitter.onComplete();
              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<String> delete(String id, PostgreSQLHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {

                HistoryContext historyContext = this.get(id, null).get();
                this.historyContextRepository.delete(historyContext);

                emitter.onNext("");
                emitter.onComplete();
              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Transactional
  public void createTable() {
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS history_context (id TEXT PRIMARY KEY, response TEXT, created_at"
            + " timestamp)");
  }
}
