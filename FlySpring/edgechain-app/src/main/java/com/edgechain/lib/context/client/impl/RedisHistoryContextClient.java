package com.edgechain.lib.context.client.impl;

import com.edgechain.lib.context.domain.HistoryContext;
import com.edgechain.lib.context.client.HistoryContextClient;
import com.edgechain.lib.endpoint.impl.RedisHistoryContextEndpoint;
import com.edgechain.lib.rxjava.transformer.observable.EdgeChain;
import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisHistoryContextClient
    implements HistoryContextClient<RedisHistoryContextEndpoint> {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final String PREFIX = "historycontext:";

  @Autowired private RedisTemplate redisTemplate;

  @Autowired @Lazy private Environment env;

  @Override
  public EdgeChain<HistoryContext> create(String id, RedisHistoryContextEndpoint endpoint) {

    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {

                if (Objects.isNull(id) || id.isEmpty())
                  throw new RuntimeException("Redis key cannot be empty or null");

                String key = PREFIX + id;

                if (this.redisTemplate.hasKey(key))
                  throw new RuntimeException("Duplicate historycontext is not allowed.");

                HistoryContext context = new HistoryContext();
                context.setId(key);
                context.setResponse("");
                context.setCreatedAt(LocalDateTime.now());

                this.redisTemplate.opsForValue().set(key, context);
                this.redisTemplate.expire(
                    key, Long.parseLong(env.getProperty("redis.ttl")), TimeUnit.SECONDS);

                emitter.onNext(context);
                emitter.onComplete();

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<HistoryContext> put(
      String key, String response, RedisHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {

                HistoryContext historyContext = this.get(key, null).get();
                historyContext.setResponse(response);

                this.redisTemplate.opsForValue().set(key, historyContext);
                this.redisTemplate.expire(
                    key, Long.parseLong(env.getProperty("redis.ttl")), TimeUnit.SECONDS);

                logger.info(String.format("%s is updated", key));

                emitter.onNext(historyContext);
                emitter.onComplete();

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<HistoryContext> get(String key, RedisHistoryContextEndpoint endpoint) {
    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {
                Boolean b = this.redisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(b)) {
                  emitter.onNext(
                      Objects.requireNonNull(
                          (HistoryContext) this.redisTemplate.opsForValue().get(key)));
                  emitter.onComplete();
                } else {
                  emitter.onError(new RuntimeException("Redis history_context id isn't found."));
                }

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }

  @Override
  public EdgeChain<String> delete(String key, RedisHistoryContextEndpoint endpoint) {

    return new EdgeChain<>(
        Observable.create(
            emitter -> {
              try {
                this.get(key, null).get();
                this.redisTemplate.delete(key);
                emitter.onNext("");
                emitter.onComplete();

              } catch (final Exception e) {
                emitter.onError(e);
              }
            }),
        endpoint);
  }
}
