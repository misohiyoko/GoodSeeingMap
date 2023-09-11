package net.gdseeing.goodseeingmap.backend;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class S3Controller {
    private String key = "AKIA56O74YJ7Z3QA5I37";
    private String secret = "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";
    private String bucket = "gsm-pictures-content";
    BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(key, secret);
    AmazonS3Client s3Client = new AmazonS3Client(basicAWSCredentials);
    APIController apiController = new APIController();
    public void upload(Context context,PictureData pictureData, File file, StringCallback stringCallback) throws JsonProcessingException {
        TransferUtility transferUtility = new TransferUtility(s3Client, context.getApplicationContext());
        TransferObserver observer = transferUtility.upload(bucket, pictureData.getPict_id(), file);
        Request request = apiController.pictUploadRequest(pictureData);
        apiController.doRequest(request, new ResponseCallback() {
            @Override
            public void onComplete(Response response) throws IOException {
                ResponseCallback.super.onComplete(response);
                Log.i("wae",response.body().string());
            }
        });
        observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.d("AwsSample", "status: "+state);
                    if (state == TransferState.COMPLETED){
                        stringCallback.onComplete(state.toString());
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    Log.d("AwsSample", "progress: "+id+" bytesCurrent:"+bytesCurrent+" bytesTotal:"+bytesTotal);
                }

                @Override
                public void onError(int id, Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
    public void download(Context context,PictureData pictureData, File file, StringCallback stringCallback){
        TransferUtility transferUtility = new TransferUtility(s3Client, context.getApplicationContext());
        TransferObserver observer = transferUtility.download(bucket, pictureData.getPict_id(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("AwsSample", "status: "+state);
                if (state == TransferState.COMPLETED){
                    stringCallback.onComplete(state.toString());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("AwsSample", "progress: "+id+" bytesCurrent:"+bytesCurrent+" bytesTotal:"+bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
