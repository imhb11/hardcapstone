package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

import java.util.HashMap;

public class Meeting_detail extends AppCompatActivity implements OnMapReadyCallback{
    private MapView mapView;
    private NaverMap mnavermap;

    private String postId; // 게시물 ID
    private TextView titleTextView; // 제목 TextView
    private TextView introTextView; // 한줄소개 TextView
    private TextView participantsTextView; // 참여인원 TextView
    private TextView contentTextView; // 본문 내용 TextView
    private ImageButton applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_detail);
        // UI 컴포넌트 초기화
        titleTextView = findViewById(R.id.meeting_title);
        introTextView = findViewById(R.id.meeting_summary);
        participantsTextView = findViewById(R.id.personnel);
        contentTextView = findViewById(R.id.meeting_text);
        applyButton = findViewById(R.id.meeting_apply);

        findViewById(R.id.check).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_detail.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_detail.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_detail.this, Chat.class);
            startActivity(intent);
        });
        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_detail.this, Mypage.class);
            startActivity(intent);
        });

        findViewById(R.id.detail_back).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_detail.this, Meeting_category.class);
            startActivity(intent);
        });

        //지도화면
        mapView = findViewById(R.id.meeting_detail_map_fragment);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //화면에 데이터 출력
        // Intent에서 데이터를 가져옴
        String postId = getIntent().getStringExtra("postId");
        String title = getIntent().getStringExtra("title");
        String introduction = getIntent().getStringExtra("introduction");
        String content = getIntent().getStringExtra("content");
        int memberCount = getIntent().getIntExtra("memberCount", 0);
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String meetingCategoryId = getIntent().getStringExtra("meetingCategoryId");
        String subcategoryId = getIntent().getStringExtra("subcategoryId");

        titleTextView.setText(title);
        introTextView.setText(introduction);
        contentTextView.setText(content);
        participantsTextView.setText(String.valueOf(memberCount));

        //소모임 신청 버튼
        applyButton.setOnClickListener(v -> {
            sendApplicationRequest(postId,meetingCategoryId,subcategoryId); // postId를 인자로 전달
        });

        addMarker(latitude, longitude);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.mnavermap = naverMap;
        // 여기에서 마커를 추가합니다.
        // Intent에서 가져온 latitude와 longitude를 사용
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        addMarker(latitude, longitude);
    }


    // 마커를 지도에 추가하는 메서드
    private void addMarker(double latitude, double longitude) {
        if (mnavermap != null) {
            Marker marker = new Marker();
            marker.setPosition(new LatLng(latitude, longitude));
            marker.setMap(mnavermap);
            mnavermap.moveCamera(CameraUpdate.scrollTo(new LatLng(latitude, longitude)));

            Log.d("Marker", "마커 추가됨: 위도=" + latitude + ", 경도=" + longitude);
        } else {
            Log.e("Marker", "mnavermap이 null입니다. 마커를 추가할 수 없습니다.");
        }
    }



    //소모임 신청
    private void sendApplicationRequest(String postId, String meetingCategoryId, String subcategoryId) {
        String url = "http://15.165.92.121:8080/meetings/" + meetingCategoryId + "/join";
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject applicationData = new JSONObject();
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null); // 기본값으로 null 설정

            applicationData.put("postId", postId);
            applicationData.put("meetingCategoryId", meetingCategoryId);
            applicationData.put("subcategoryId", subcategoryId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                applicationData,
                response -> {
                    // 서버에서 성공 응답을 받은 경우
                    Toast.makeText(this, "신청이 되었습니다.", Toast.LENGTH_SHORT).show();
                    // 필요에 따라 UI 업데이트 (예: 버튼 상태 변경)
                },
                error -> {
                    // 서버 요청 실패 처리
                    Toast.makeText(this, "신청 실패", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                // SharedPreferences에서 JWT 토큰 가져오기
                String jwtToken = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("jwtToken", null);
                headers.put("Authorization", "Bearer " + jwtToken); // JWT 토큰 추가

                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
