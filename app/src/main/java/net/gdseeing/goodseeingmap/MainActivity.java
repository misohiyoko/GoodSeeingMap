package net.gdseeing.goodseeingmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.gdseeing.goodseeingmap.backend.APIController;
import net.gdseeing.goodseeingmap.backend.PictureData;
import net.gdseeing.goodseeingmap.backend.ResponseCallback;
import net.gdseeing.goodseeingmap.backend.S3Controller;
import net.gdseeing.goodseeingmap.backend.StringCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        S3Controller s3Controller = new S3Controller();
        APIController apiController = new APIController();
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