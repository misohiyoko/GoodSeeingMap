package net.gdseeing.goodseeingmap.backend_connection;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIController {
    String url = "https://iugrul57ej.execute-api.ap-southeast-2.amazonaws.com/v1/picture";
    OkHttpClient client = new OkHttpClient();
    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public Request pictUploadRequest(PictureData pictureData) throws JsonProcessingException {
        RequestBody body = RequestBody.create( pictureData.json(),JSON);
        return new Request.Builder().url(url).post(body).build();
    }
    public Request pictUpdateRequest(PictureData pictureData) throws JsonProcessingException {
        RequestBody body = RequestBody.create(pictureData.json(),JSON);
        return new Request.Builder().url(url).put(body).build();
    }
    public Request pictDeleteRequest(PictureData pictureData) throws JsonProcessingException {
        RequestBody body = RequestBody.create(pictureData.json(),JSON);
        return new Request.Builder().url(url).delete(body).build();
    }
    public Request pictGetRangeRequest(CoordinateRange coordinateRange) throws JsonProcessingException {

        RequestBody body = RequestBody.create(coordinateRange.json(),JSON);
        return new Request.Builder().url(url).delete(body).build();
    }
    public void doRequest(Request request, ResponseCallback responseCallback) throws JsonProcessingException {

        ExecutorService executorService;
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    responseCallback.onComplete(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }





}
