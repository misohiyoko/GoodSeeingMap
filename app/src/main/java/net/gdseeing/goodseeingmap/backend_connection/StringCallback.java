package net.gdseeing.goodseeingmap.backend_connection;

import java.io.IOException;

public interface StringCallback {
    default void onComplete(String str) throws IOException {}
}
