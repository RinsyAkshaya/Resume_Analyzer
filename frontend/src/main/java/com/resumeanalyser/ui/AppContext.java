package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.resumeanalyser.api.ApiClient;

/** State shared by all screens: API client, logged-in user and the last uploaded resume. */
public class AppContext {

    public final ApiClient api;
    public MainFrame frame;
    public AppShell shell;

    public long userId;
    public String fullName = "";
    public String email = "";

    public Long lastResumeId;
    public String lastResumeLabel = "";

    public AppContext(ApiClient api) {
        this.api = api;
    }

    public void setUser(JsonNode user) {
        userId = user.path("userId").asLong();
        fullName = user.path("fullName").asText();
        email = user.path("email").asText();
    }

    public void clear() {
        userId = 0;
        fullName = "";
        email = "";
        lastResumeId = null;
        lastResumeLabel = "";
    }
}
