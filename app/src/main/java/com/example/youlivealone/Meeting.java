package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.view.View;
import android.widget.ImageButton;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.List;

public class Meeting extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private NaverMap mnavermap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource; //위치 반환 구현체
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences 파일 이름
    private static final String KEY_CATEGORY_ID = "categoryId"; // 저장할 키


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting);

        //지도 불러오기
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("m5c61qtjs2")
        );

        //위치 표시
        mapView = findViewById(R.id.meeting_map_fragment);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);



        //하단바 버튼 작동코드들
        findViewById(R.id.check).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, Chat.class);
            startActivity(intent);
        });
        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, Mypage.class);
            startActivity(intent);
        });
        findViewById(R.id.fullscreen_map).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, Map.class);
            startActivity(intent);
        });
        findViewById(R.id.meeting_back1).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting.this, MainActivity.class);
            startActivity(intent);
        });


        //카테고리 버튼 클릭 시
        GridLayout categorygridLayout = findViewById(R.id.gathering_category);
        for (int i = 0; i < categorygridLayout.getChildCount(); i++) {
            View child = categorygridLayout.getChildAt(i);
            if (child instanceof ImageButton) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // 각 버튼에 맞는 카테고리 ID를 지정
                        int categoryId = -1;
                        if (v.getId() == R.id.imageButton2) {
                            categoryId = 1; // 운동
                        } else if (v.getId() == R.id.imageButton3) {
                            categoryId = 2; // 친목
                        } else if (v.getId() == R.id.imageButton4) {
                            categoryId = 3; // 동창회
                        } else if (v.getId() == R.id.imageButton6) {
                            categoryId = 4; // 음식
                        } else if (v.getId() == R.id.imageButton5) {
                            categoryId = 5; // 스터디
                        } else if (v.getId() == R.id.imageButton7) {
                            categoryId = 6; // 문화
                        }


                        // 카테고리 ID를 SharedPreferences에 저장
                        // 카테고리 ID가 유효한 경우에만 저장
                        if (categoryId != -1) {
                            editor.putInt(KEY_CATEGORY_ID, categoryId);
                            editor.apply();
                        }

                        Intent intent = new Intent(Meeting.this, Meeting_category.class);
                        // 각 버튼에 맞는 데이터를 인텐트에 추가
                        intent.putExtra("buttonId", categoryId);
                        startActivity(intent);
                    }
                });
            }
        }
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
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);;

    }



}
