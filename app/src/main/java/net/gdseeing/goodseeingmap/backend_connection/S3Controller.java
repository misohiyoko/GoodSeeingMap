package net.gdseeing.goodseeingmap.backend_connection;

import android.content.Context;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
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
    ClientConfiguration config = new ClientConfiguration();


    AmazonS3Client s3Client;
    APIController apiController = new APIController();
    TransferUtility transferUtility;
    public S3Controller(Context context){
        transferUtility = new TransferUtility(s3Client, context.getApplicationContext());
        config.setMaxConnections(128);
        config.setSocketTimeout(120000);
        config.setConnectionTimeout(60000);
        config.setMaxErrorRetry(3);
        config.setSocketBufferSizeHints(10240,10240);
        s3Client = new AmazonS3Client(basicAWSCredentials,config);
    }

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
                        try {
                            stringCallback.onComplete(state.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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

        TransferObserver observer = transferUtility.download(bucket, pictureData.getPict_id(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("AwsSample", "status: "+state);
                if (state == TransferState.COMPLETED){
                    try {
                        stringCallback.onComplete(state.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
