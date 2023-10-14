package net.gdseeing.goodseeingmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.gdseeing.goodseeingmap.backend_connection.APIController;
import net.gdseeing.goodseeingmap.backend_connection.PictureData;
import net.gdseeing.goodseeingmap.backend_connection.ResponseCallback;
import net.gdseeing.goodseeingmap.backend_connection.S3Controller;
import net.gdseeing.goodseeingmap.backend_connection.StringCallback;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.Response;

public class Post extends AppCompatActivity {
    private static final int RESULT_PICK_IMAGEFILE = 1000;
    private ImageView imageView;
    private EditText et_place;
    private EditText et_input;
    private float[] pict_lat_lng;
    @Nullable private Uri set_uri = null;
    public S3Controller s3Controller;
    public APIController apiController;

    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);
        imageView = (ImageView) findViewById(R.id.iv_pic);
        et_place = (EditText) findViewById(R.id.et_place);
        et_input = (EditText) findViewById(R.id.et_input);
        this.s3Controller = new S3Controller(getApplicationContext());
        this.apiController = new APIController();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        findViewById(R.id.ib_chooce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);


            }
        });
        findViewById(R.id.ib_post).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(set_uri != null){
                        String uuidString = UUID.randomUUID().toString();

                        PictureData pictData = new PictureData(uuidString,et_input.getText().toString(), pict_lat_lng[0],pict_lat_lng[1],"","http://tobeused.com","preRelease");
                        progressBar.setVisibility(View.VISIBLE);
                        try {
                            if(uploadPicture(pictData,set_uri)){

                            }
                        } catch (IOException e) {
                            progressBar.setVisibility(View.GONE);
                            throw new RuntimeException(e);
                        }
                    }
                }
        });
        findViewById(R.id.ib_returun).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(net.gdseeing.goodseeingmap.Post.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->  {

        if (result.getResultCode() == RESULT_OK) {
            Uri uri = null;
            if (result.getData() != null) {
                uri = result.getData().getData();

                try {
                    InputStream stream = getApplicationContext().getContentResolver().openInputStream(uri);

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
                    ExifInterface exifInterface = new ExifInterface(bufferedInputStream);
                    float[] latLng = new float[2];
                    exifInterface.getLatLong(latLng);
                    if(latLng[0] != 0.0){
                        et_place.setText(Float.toString(latLng[0]) + "," + Float.toString(latLng[1]));
                        pict_lat_lng = latLng;
                    }


                    Bitmap bmp = getBitmapFromUri(uri);
                    imageView.setImageBitmap(bmp);
                    set_uri = uri;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    // 画像終わり
    private Boolean uploadPicture(PictureData pictureData, Uri uri) throws IOException {
        try {
            Request request = apiController.pictUploadRequest(pictureData);
            apiController.doRequest(request, new ResponseCallback() {
                @Override
                public void onComplete(Response response) throws IOException {
                    ResponseCallback.super.onComplete(response);
                    Log.i("wae",response.body().string());
                }
            });
        } catch (JsonProcessingException e) {
            Log.e("Error", e.toString());
            return false;
        }
        Bitmap bitmap = getBitmapFromUri(uri);
        File resizedFile = File.createTempFile(pictureData.getPict_id(), null, getApplicationContext().getCacheDir());
        FileOutputStream outStream = new FileOutputStream(resizedFile);
        bitmap = resize(bitmap, 1280, 1280);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
        outStream.close();
        bitmap.recycle();
        File uploadFile = resizedFile;

        try {
            s3Controller.upload(getApplicationContext(), pictureData, uploadFile, new StringCallback() {
                @Override
                public void onComplete(String str) throws IOException, JSONException {
                    StringCallback.super.onComplete(str);
                    Log.i("wae",str);
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(net.gdseeing.goodseeingmap.Post.this, MainActivity.class);
                    startActivity(intent);
                }
            });
        } catch (JsonProcessingException e) {
            Log.e("Error", e.toString());
            return false;
        }
        return true;
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int cindex = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
