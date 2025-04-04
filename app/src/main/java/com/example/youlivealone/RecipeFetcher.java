package com.example.youlivealone;

import okhttp3.*;
import android.util.Log;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

public class RecipeFetcher {

    private static final String GPT_API_URL = "https://api.openai.com/v1/completions";
    private static final String TAG = "RecipeFetcher";

    public void fetchRecipe(List<String> ingredients, String apiKey, Callback callback) {
        OkHttpClient client = new OkHttpClient(); // connectionSpecs 설정 없이 기본 구성 사용

        String prompt = "Using ingredients: " + String.join(", ", ingredients) + ", suggest a recipe.";
        String json = "{"
                + "\"model\": \"gpt-4-0613\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],"
                + "\"max_tokens\": 150"
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Log.d(TAG, "Requesting recipe with prompt: " + prompt);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response received: " + responseBody);
                    callback.onResponse(call, response);
                } else {
                    Log.e(TAG, "Request unsuccessful. Response code: " + response.code());
                }
            }
        });
    }
}
