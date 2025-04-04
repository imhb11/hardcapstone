package com.example.youlivealone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Smart extends AppCompatActivity implements IngredientAnalyzer.IngredientDetectionCallback {

    private Interpreter interpreter;
    private IngredientAnalyzer ingredientAnalyzer;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private Handler handler;
    private ImageView detectedImageView; // 탐지된 이미지를 표시할 ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smart);

        previewView = findViewById(R.id.previewView);
        detectedImageView = findViewById(R.id.detectedImageView); // 레이아웃에서 ImageView 연결

        // 카메라 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            initializeComponents();
        }
    }

    private void initializeComponents() {
        try {
            // 모델 파일 로드 및 라벨 설정
            MappedByteBuffer tfliteModel = loadModelFile("ingredient_model.tflite");
            List<String> labels = Arrays.asList("콩나물", "소고기", "닭고기", "달걀", "돼지고기", "마늘", "대파", "김치", "양파", "감자", "스팸");
            interpreter = new Interpreter(tfliteModel);

            // IngredientAnalyzer 초기화
            ingredientAnalyzer = new IngredientAnalyzer(interpreter, labels, this);
            startCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCamera();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera() {
        if (cameraProvider == null) return;

        cameraProvider.unbindAll();

        // CameraX Preview 설정
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // ImageAnalysis 설정
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ingredientAnalyzer);

        // 카메라 바인딩
        cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
    }

    @Override
    public void onIngredientsDetected(List<String> ingredients) {
        // 특정 재료가 인식되면 카메라 분석 중지 및 이미지 표시
        if (ingredients.contains("콩나물")) {
            // 분석 중지
            if (cameraProvider != null) {
                cameraProvider.unbind(imageAnalysis);
            }

            // 3초 지연 후 kongnamul 이미지 표시
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                detectedImageView.setImageResource(R.drawable.kongnamul); // kongnamul 이미지를 표시
                detectedImageView.setVisibility(ImageView.VISIBLE);
            }, 3000); // 3초 지연
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeComponents();
        }
    }
}
