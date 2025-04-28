package com.example.youlivealone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class Purchase extends AppCompatActivity {

    String url = "https://m.market09.kr/home";
    String msg;
    final Bundle bundle = new Bundle();
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String message = bundle.getString("message");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase);

        //버튼 작동코드들

        findViewById(R.id.map).setOnClickListener(v -> {
            Intent intent = new Intent(Purchase.this, Map.class);
            startActivity(intent);
        });

        findViewById(R.id.check).setOnClickListener(v -> {
            Intent intent = new Intent(Purchase.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Purchase.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            Intent intent = new Intent(Purchase.this, Chat.class);
            startActivity(intent);
        });
        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Purchase.this, Mypage.class);
            startActivity(intent);
        });

//        new Thread(){
//            @Override
//            public void run(){
//                Document doc = null;
//                try{
//                    doc = Jsoup.connect(url).get();
//                    msg = doc.text();
//                    bundle.putString("message",msg);
//                    Message msg = handler.obtainMessage();
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                } catch(IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }.start();
        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 활성화
        webView.getSettings().setDomStorageEnabled(true);  // DOM 스토리지 (localStorage) 허용
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 9; SM-G960F Build/PPR1.180610.011) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.110 Mobile Safari/537.36");

        webView.loadUrl("https://m.market09.kr/home");
    }
}