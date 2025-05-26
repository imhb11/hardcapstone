package com.example.youlivealone;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatService {
    @POST("/counsel/cha")
    Call<ChatResponse> getResponse(
            @Body ChatRequest request
    );
}

