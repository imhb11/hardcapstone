package com.example.youlivealone;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import org.tensorflow.lite.Interpreter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class IngredientAnalyzer implements ImageAnalysis.Analyzer {

    private final Interpreter interpreter;
    private final List<String> labels;
    private static final int INPUT_SIZE = 320;  // 모델이 기대하는 입력 크기 (320x320)
    private static final float CONFIDENCE_THRESHOLD = 0.3f;
    private IngredientDetectionCallback callback;

    public interface IngredientDetectionCallback {
        void onIngredientsDetected(List<String> ingredients);
    }

    public IngredientAnalyzer(Interpreter interpreter, List<String> labels, IngredientDetectionCallback callback) {
        this.interpreter = interpreter;
        this.labels = labels;
        this.callback = callback;
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
        ByteBuffer tfInput = processImageToTensor(imageProxy);

        float[][][] output = new float[1][6300][16];
        interpreter.run(tfInput, output);

        List<String> detectedIngredients = processModelOutput(output);

        if (!detectedIngredients.isEmpty()) {
            callback.onIngredientsDetected(detectedIngredients);
        }

        imageProxy.close();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private ByteBuffer processImageToTensor(ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null) {
            return ByteBuffer.allocateDirect(0);
        }

        Bitmap bitmap = imageToBitmap(image);
        return convertBitmapToByteBuffer(bitmap);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        for (int pixel : intValues) {
            byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
            byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);
            byteBuffer.putFloat((pixel & 0xFF) / 255.0f);
        }

        return byteBuffer;
    }

    private List<String> processModelOutput(float[][][] output) {
        List<String> detectedIngredients = new ArrayList<>();

        for (int i = 0; i < 6300; i++) {
            float[] detection = output[0][i];
            int classIndex = getMaxIndex(detection);
            float classProbability = detection[classIndex];

            if (classIndex < labels.size() && classProbability > CONFIDENCE_THRESHOLD) {
                String detectedLabel = labels.get(classIndex);
                if (!detectedIngredients.contains(detectedLabel)) {
                    detectedIngredients.add(detectedLabel);
                }
                Log.d("IngredientAnalyzer", "Detected: " + detectedLabel + " with confidence: " + classProbability);
            }
        }
        return detectedIngredients;
    }

    private int getMaxIndex(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private Bitmap imageToBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer rgbBuffer = planes[0].getBuffer();

        buffer.put(rgbBuffer);
        buffer.rewind();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }
}
