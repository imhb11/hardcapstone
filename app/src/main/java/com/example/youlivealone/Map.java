package com.example.youlivealone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


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
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource; //위치 반환 구현체
    private NaverMap mnavermap;
    private MapView mapView;
    private RequestQueue requestQueue;
    private ArrayList<Marker> markers = new ArrayList<>(); // 마커 리스트
    private int selectedCategoryId; // 선택된 카테고리 ID
    private ArrayList<JSONObject> allMeetings = new ArrayList<>(); // 모든 소모임 데이터 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        //지도 불러오기
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("m5c61qtjs2")
        );

        //위치 표시
        mapView = findViewById(R.id.map_fragment);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        fetchAllMeetings(); // 모든 소모임 데이터 가져오기
        setUpButtonListeners(); // 여기에서 버튼 리스너를 설정합니다.


        // 버튼 작동코드들

        findViewById(R.id.back).setOnClickListener(v -> {
            Intent intent = new Intent(Map.this, Meeting_detail.class);
            startActivity(intent);
        });

    }

    //권한 여부처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,grantResults )){
            if (!locationSource.isActivated()){
                mnavermap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        //위치 표시
        this.mnavermap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
    }

    // 카테고리 선택 시 해당 카테고리에 맞는 마커 표시
    private void displayMarkersForCategory(int categoryId) {
        // 기존 마커 삭제
        for (Marker marker : markers) {
            marker.setMap(null);
        }
        markers.clear();

        try {
            for (JSONObject meeting : allMeetings) {
                int meetingCategoryId = meeting.getInt("meetingCategoryId");
                if (meetingCategoryId == categoryId) {
                    double lat = meeting.getDouble("latitude");
                    double lng = meeting.getDouble("longitude");
                    String title = meeting.getString("title");

                    // 지도에 마커 추가
                    Marker marker = new Marker();
                    marker.setPosition(new LatLng(lat, lng));
                    marker.setMap(mnavermap);
                    marker.setCaptionText(title);
                    marker.setCaptionTextSize(12);
                    markers.add(marker); // 마커 리스트에 추가
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // 버튼 클릭 리스너
    private void setUpButtonListeners() {
        int[] buttonIds = {R.id.btnexercise, R.id.btnchinmok, R.id.btndongchang, R.id.btnfood, R.id.btnstudy, R.id.btnmovie};
        int[] categoryIds = {1, 2, 3, 4, 5, 6};

        for (int i = 0; i < buttonIds.length; i++) {
            final int categoryId = categoryIds[i];
            findViewById(buttonIds[i]).setOnClickListener(v -> {
                selectedCategoryId = categoryId;
                fetchAllMeetings();
            });
        }
    }


    private void fetchAllMeetings() {
        String url = "http://15.165.92.121:8080/meetings/locations"; // 서버 URL
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // 모든 회의 데이터를 가져오기
                            allMeetings.clear(); // 이전 데이터 초기화
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject meeting = response.getJSONObject(i);
                                allMeetings.add(meeting);
                            }

                            // 카테고리가 선택되어 있다면 마커 표시
                            if (selectedCategoryId != 0) {
                                displayMarkersForCategory(selectedCategoryId);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
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