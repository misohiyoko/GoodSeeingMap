package net.gdseeing.goodseeingmap.backend_connection;

import java.io.IOException;

import okhttp3.Response;

public interface ResponseCallback {
    default void onComplete(Response response) throws IOException {}
}
