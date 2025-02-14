package com.edgechain.lib.openai.request;

import com.edgechain.lib.response.ArkObject;
import org.json.JSONObject;

public class ChatMessage implements ArkObject {
  String role;
  String content;

  public ChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }

  public ChatMessage() {}

  public String getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "ChatMessage{" + "role='" + role + '\'' + ", content='" + content + '\'' + '}';
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("role", role);
    json.put("content", content);
    return json;
  }
}
