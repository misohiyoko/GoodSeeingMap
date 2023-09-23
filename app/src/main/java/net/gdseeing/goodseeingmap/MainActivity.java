package net.gdseeing.goodseeingmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.gdseeing.goodseeingmap.backend_connection.APIController;
import net.gdseeing.goodseeingmap.backend_connection.PictureData;
import net.gdseeing.goodseeingmap.backend_connection.ResponseCallback;
import net.gdseeing.goodseeingmap.backend_connection.S3Controller;
import net.gdseeing.goodseeingmap.backend_connection.StringCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public S3Controller s3Controller;
    public APIController apiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.s3Controller = new S3Controller();
        this.apiController = new APIController();
        ImageButton buttonSearch = findViewById(R.id.search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SecondActivity に遷移する Intent を作成
                Intent intent = new Intent(net.gdseeing.goodseeingmap.MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

        // post画面へ遷移
        Button buttonPost = findViewById(R.id.post);
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SecondActivity に遷移する Intent を作成
                Intent intent = new Intent(net.gdseeing.goodseeingmap.MainActivity.this, Post.class);
                startActivity(intent);
            }
        });

        PictureData pictData = new PictureData("asdw","bp", 1.2F,1.4F,"kgj","http://ho","arer");
        try {
            Request request = apiController.pictUploadRequest(pictData);
            apiController.doRequest(request, new ResponseCallback() {
                @Override
                public void onComplete(Response response) throws IOException {
                    ResponseCallback.super.onComplete(response);
                    Log.i("wae",response.body().string());
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        File uploadFile = null;
        try {
            uploadFile = File.createTempFile("upload-sample", "txt");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(uploadFile))) {
                bw.write("test");
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            s3Controller.upload(getApplicationContext(), pictData, uploadFile, new StringCallback() {
                @Override
                public void onComplete(String str) {
                    StringCallback.super.onComplete(str);
                    Log.i("wae",str);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}