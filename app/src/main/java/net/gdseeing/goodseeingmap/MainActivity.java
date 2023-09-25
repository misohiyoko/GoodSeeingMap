package net.gdseeing.goodseeingmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

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
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //⑤b 権限が必要な理由・メリットを説明
            builder.setMessage(R.string.alert_dialog)
                    .setPositiveButton("Yes", (dialog, id) ->
                            requestPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION))
                    .setNegativeButton("No", (dialog, id) ->
                            android.os.Process.killProcess(android.os.Process.myPid()));
            builder.create();
            builder.show();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION

            );
        }


    }
}