package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class community_post extends AppCompatActivity {
    private ImageButton likeButton;
    private String postId; // 게시물 ID
    private TextView likesCountView; // 좋아요 수를 표시할 TextView
    private ArrayAdapter<String> commentsAdapter; // 댓글 어댑터
    private ArrayList<String> commentsList; // 댓글 리스트
    private ListView commentsListView; // 댓글을 표시할 ListView
    private EditText commentInput; // 댓글 입력 필드


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_post);

        TextView titleView = findViewById(R.id.title);
        TextView contentView = findViewById(R.id.post_content);
//        TextView userIdView = findViewById(R.id.userid);
        commentsListView = findViewById(R.id.comment_list); // XML에서 댓글 표시할 TextView 가져오기
        commentInput = findViewById(R.id.comment_input); // EditText 초기화

        Intent intent = getIntent();
        Post selectedPost = (Post) intent.getSerializableExtra("selectedPost");
        postId = selectedPost.getId(); // 게시물 ID 가져오기

        // 데이터 설정
        titleView.setText(selectedPost.getTitle());
//        userIdView.setText(String.valueOf(selectedPost.getUserId())); // 사용자 ID 출력
        contentView.setText(selectedPost.getContent()); // 내용 출력

        likeButton = findViewById(R.id.goodbutton); // XML에서 버튼 ID에 맞춰 가져오기

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLikeRequest();
            }
        });

        // 댓글 보내기 버튼 클릭 리스너
        findViewById(R.id.comment_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = commentInput.getText().toString(); // 댓글 내용 가져오기
                if (!content.isEmpty()) {
                    postComment(postId, content); // 댓글 보내기
                }
            }
        });


        // 댓글 리스트 초기화
        commentsList = new ArrayList<>();
        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentsList);
        commentsListView.setAdapter(commentsAdapter);

        likesCountView = findViewById(R.id.goodcount); // IDs를 적절히 변경


        fetchLikesCount(); // 좋아요 수 가져오기
        getComments(postId); // 댓글 불러오기
    }
    //좋아요 수
    private void fetchLikesCount() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://15.165.92.121:8080/posts/" + postId + "/like-count"; // 서버 URL 설정

        // GET 요청 생성
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // 응답이 정수 값으로 올 경우
                            int likesCount = Integer.parseInt(response.trim()); // 문자열을 정수로 변환
                            likesCountView.setText(String.valueOf(likesCount)); // 정수를 문자열로 변환하여 TextView에 표시
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            likesCountView.setText("0"); // 에러 시 기본값으로 0 표시

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 오류 발생 시 처리
                        System.out.println("Error: " + error.toString());
                    }
                });

        // 요청을 큐에 추가
        queue.add(stringRequest);
    }


    //좋아요
    private void sendLikeRequest() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null); // JWT 토큰

        if (token == null) {
            // 토큰이 없을 경우 처리
            System.out.println("JWT 토큰이 없습니다.");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://15.165.92.121:8080/posts/" + postId + "/like"; // 서버 URL 설정

        // POST 요청 생성
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 성공적으로 응답을 받았을 때 처리
                        Toast.makeText(community_post.this, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 오류 발생 시 처리
                        Log.e("Error", error.toString());
                        Toast.makeText(community_post.this, "좋아요 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // JWT 토큰을 헤더에 포함
                return headers;
            }

            @Override
            public byte[] getBody() {
                // 본문을 비워서 보냄
                return new byte[0]; // 빈 배열을 반환하여 본문을 비움
            }
        };

        // 요청을 큐에 추가
        queue.add(stringRequest);
    }



    // 댓글 보내기
    public void postComment(String postId, String content) {
        String url = "http://15.165.92.121:8080/comments";

        // SharedPreferences에서 JWT 토큰 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString("jwtToken", null); // 토큰 키 수정


        // JSON 객체 생성
        JSONObject jsonBody = new JSONObject();
        try {
            // JSON 구조 수정: categoryId, postId, content 포함
            jsonBody.put("categoryId", 1); // 예시로 categoryId를 1로 설정, 실제 값으로 대체 필요
            jsonBody.put("postId", postId); // 게시물 ID
            jsonBody.put("content", content); // 댓글 내용
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Volley 요청 큐 생성
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // 서버로부터의 응답 처리
                        try {
                            String responseContent = response.getString("content"); // 응답에서 댓글 내용 가져오기
                            commentsList.add(responseContent); // 댓글 리스트에 추가
                            commentsAdapter.notifyDataSetChanged(); // 리스트 업데이트
                            commentInput.setText(""); // 입력 필드 초기화
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                        System.out.println("Error: " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken); // JWT 토큰을 헤더에 추가
                System.out.println("JWT Token Sent: Bearer " + jwtToken); // 전송하는 토큰을 로그로 확인
                return headers;
            }
        };

        // 요청 큐에 추가
        // postComment 메서드 내에 아래 로그 추가
        System.out.println("Post ID: " + postId); // 전송하는 postId 확인
        System.out.println("Comment Content: " + content); // 전송하는 content 확인

        requestQueue.add(jsonObjectRequest);
    }




    //댓글 불러오기
    public void getComments(String postId) {
        String url = "http://15.165.92.121:8080/comments/post/"+ postId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // 서버로부터의 응답 처리
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject comment = response.getJSONObject(i);
                                String content = comment.getString("content");
                                commentsList.add(content); // 댓글 리스트에 추가
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        commentsAdapter.notifyDataSetChanged(); // 리스트 업데이트
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 에러 처리
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

}
