package com.edgechain.lib.endpoint.impl;

import com.edgechain.lib.configuration.context.ApplicationContextHolder;
import com.edgechain.lib.embeddings.WordEmbeddings;
import com.edgechain.lib.openai.request.ChatMessage;
import com.edgechain.lib.request.ArkRequest;
import com.edgechain.lib.retrofit.client.OpenAiStreamService;
import com.edgechain.lib.retrofit.OpenAiService;
import com.edgechain.lib.endpoint.Endpoint;
import com.edgechain.lib.openai.response.ChatCompletionResponse;
import com.edgechain.lib.retrofit.client.RetrofitClientInstance;
import com.edgechain.lib.rxjava.retry.RetryPolicy;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;

import java.util.List;
import java.util.Objects;

public class OpenAiEndpoint extends Endpoint {

  private final OpenAiStreamService openAiStreamService =
      ApplicationContextHolder.getContext().getBean(OpenAiStreamService.class);

  private final Retrofit retrofit = RetrofitClientInstance.getInstance();
  private final OpenAiService openAiService = retrofit.create(OpenAiService.class);

  private String orgId;
  private String model;
  private String role;
  private Double temperature;

  private Boolean stream;

  /** Getter Fields ** */
  private List<ChatMessage> chatMessages;

  private String input;

  /** Log fields * */
  private String chainName;

  private String callIdentifier;

  public OpenAiEndpoint() {}

  public OpenAiEndpoint(String url, String apiKey, String model) {
    super(url, apiKey, null);
    this.model = model;
  }

  public OpenAiEndpoint(String url, String apiKey, String model, RetryPolicy retryPolicy) {
    super(url, apiKey, retryPolicy);
    this.model = model;
  }

  public OpenAiEndpoint(
      String url, String apiKey, String model, String role, RetryPolicy retryPolicy) {
    super(url, apiKey, retryPolicy);
    this.model = model;
    this.role = role;
  }

  public OpenAiEndpoint(
      String url,
      String apiKey,
      String model,
      String role,
      Double temperature,
      RetryPolicy retryPolicy) {
    super(url, apiKey, retryPolicy);
    this.model = model;
    this.role = role;
    this.temperature = temperature;
  }

  public OpenAiEndpoint(
      String url, String apiKey, String model, String role, Double temperature, Boolean stream) {
    super(url, apiKey, null);
    this.model = model;
    this.role = role;
    this.temperature = temperature;
    this.stream = stream;
  }

  public OpenAiEndpoint(
      String url,
      String apiKey,
      String model,
      String role,
      Double temperature,
      Boolean stream,
      RetryPolicy retryPolicy) {
    super(url, apiKey, retryPolicy);
    this.model = model;
    this.role = role;
    this.temperature = temperature;
    this.stream = stream;
  }

  public OpenAiEndpoint(
      String url,
      String apiKey,
      String orgId,
      String model,
      String role,
      Double temperature,
      Boolean stream) {
    super(url, apiKey, null);
    this.orgId = orgId;
    this.model = model;
    this.role = role;
    this.temperature = temperature;
    this.stream = stream;
  }

  public OpenAiEndpoint(
      String url,
      String apiKey,
      String orgId,
      String model,
      String role,
      Double temperature,
      Boolean stream,
      RetryPolicy retryPolicy) {
    super(url, apiKey, retryPolicy);
    this.orgId = orgId;
    this.model = model;
    this.role = role;
    this.temperature = temperature;
    this.stream = stream;
  }

  public String getModel() {
    return model;
  }

  public String getRole() {
    return role;
  }

  public Double getTemperature() {
    return temperature;
  }

  public Boolean getStream() {
    return stream;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public void setStream(Boolean stream) {
    this.stream = stream;
  }

  public List<ChatMessage> getChatMessages() {
    return chatMessages;
  }

  public String getInput() {
    return input;
  }

  public String getChainName() {
    return chainName;
  }

  public void setChainName(String chainName) {
    this.chainName = chainName;
  }

  public String getCallIdentifier() {
    return callIdentifier;
  }

  public Observable<ChatCompletionResponse> chatCompletion(
      String input, String chainName, ArkRequest arkRequest) {

    this.chatMessages = List.of(new ChatMessage(this.role, input));
    this.chainName = chainName;

    if (Objects.nonNull(arkRequest)) {
      this.callIdentifier = arkRequest.getRequestURI();
    }

    if (Objects.nonNull(this.getStream()) && this.getStream())
      return this.openAiStreamService
          .chatCompletion(this)
          .map(
              chatResponse -> {
                if (!Objects.isNull(chatResponse.getChoices().get(0).getFinishReason())) {
                  chatResponse.getChoices().get(0).getMessage().setContent("");
                  return chatResponse;
                } else return chatResponse;
              });
    else return Observable.fromSingle(this.openAiService.chatCompletion(this));
  }

  public Observable<ChatCompletionResponse> chatCompletion(
      List<ChatMessage> chatMessages, String chainName, ArkRequest arkRequest) {

    this.chainName = chainName;
    this.chatMessages = chatMessages;

    if (Objects.nonNull(arkRequest)) {
      this.callIdentifier = arkRequest.getRequestURI();
    } else {
      this.callIdentifier = "URI wasn't provided";
    }

    if (Objects.nonNull(this.getStream()) && this.getStream())
      return this.openAiStreamService
          .chatCompletion(this)
          .map(
              chatResponse -> {
                if (!Objects.isNull(chatResponse.getChoices().get(0).getFinishReason())) {
                  chatResponse.getChoices().get(0).getMessage().setContent("");
                  return chatResponse;
                } else return chatResponse;
              });
    else return Observable.fromSingle(this.openAiService.chatCompletion(this));
  }

  public Observable<WordEmbeddings> embeddings(String input, ArkRequest arkRequest) {
    this.input = input; // set Input

    if (Objects.nonNull(arkRequest)) {
      this.callIdentifier = arkRequest.getRequestURI();
    }

    return Observable.fromSingle(
        openAiService
            .embeddings(this)
            .map(
                embeddingResponse ->
                    new WordEmbeddings(input, embeddingResponse.getData().get(0).getEmbedding())));
  }
}
