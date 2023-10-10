package net.gdseeing.goodseeingmap;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;

import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;

import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;


import net.gdseeing.goodseeingmap.backend_connection.APIController;
import net.gdseeing.goodseeingmap.backend_connection.CoordinateRange;
import net.gdseeing.goodseeingmap.backend_connection.PictureData;
import net.gdseeing.goodseeingmap.backend_connection.ResponseCallback;
import net.gdseeing.goodseeingmap.backend_connection.S3Controller;
import net.gdseeing.goodseeingmap.backend_connection.StringCallback;
import net.gdseeing.goodseeingmap.databinding.MapBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import okhttp3.Request;
import okhttp3.Response;

public class SecondActivity extends AppCompatActivity {

    MapView mapView;
    private ViewAnnotationManager viewAnnotationManager;
    private MapboxMap mapboxMap;

    ObjectMapper mapper = new ObjectMapper();
    public S3Controller s3Controller;
    public APIController apiController;

    String styleUri = "https://api.mapbox.com/styles/v1/mapbox/satellite-v9/static/131.0511,32.6405,5.32,0/300x200?";
    FloatingActionButton floatingActionButton;
    private final ActivityResultLauncher<String>activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(SecondActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(SecondActivity.this,"Permission not Granted!",Toast.LENGTH_SHORT).show();
            }
        }
    });

    private  final OnIndicatorBearingChangedListener noIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {

        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(20.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener(){
        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(noIndicatorBearingChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {

        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        MapBinding binding = MapBinding.inflate(getLayoutInflater());
        this.viewAnnotationManager = binding.mapview.getViewAnnotationManager();
        this.s3Controller = new S3Controller(getApplicationContext());
        this.apiController = new APIController();

        mapView = findViewById(R.id.mapview);


        ImageButton buttonHome = findViewById(R.id.home);
        floatingActionButton = findViewById(R.id.focusLocation);
        floatingActionButton.hide();

        if (ActivityCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }


        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SecondActivity に遷移する Intent を作成
                Intent intent = new Intent(net.gdseeing.goodseeingmap.SecondActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                LocationPuck2D locationPuck2D = new LocationPuck2D();
                locationPuck2D.setBearingImage(AppCompatResources.getDrawable(SecondActivity.this,R.drawable.baseline_location_on_24));
                locationComponentPlugin.setLocationPuck(locationPuck2D);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                locationComponentPlugin.addOnIndicatorBearingChangedListener(noIndicatorBearingChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);

                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        locationComponentPlugin.addOnIndicatorBearingChangedListener(noIndicatorBearingChangedListener);
                        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        floatingActionButton.hide();
                    }
                });
            }

        });

        CoordinateRange coordinateRange = new CoordinateRange();
        coordinateRange.setLongitude_l(120.0f);
        coordinateRange.setLongitude_h(150.0f);
        coordinateRange.setLatitude_l(20.0f);
        coordinateRange.setLatitude_h(45.0f);
        this.putPinFromCoordinateRange(coordinateRange);

    }
    private int putPinFromCoordinateRange(CoordinateRange coordinateRange){
        Handler handler = new Handler();
        try {
            Request request = apiController.pictGetRangeRequest(coordinateRange);
            apiController.doRequest(request, new ResponseCallback() {
                @Override
                public void onComplete(Response response) throws IOException {
                    AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                    String  responseBody = mapper.readTree(response.body().string()).get("body").textValue();
                    List<PictureData> pictureDataList = mapper.readValue(responseBody, new TypeReference<List<PictureData>>(){});
                    for (PictureData pict : pictureDataList){
                        File file = new File( getApplicationContext().getCacheDir(), pict.getPict_id());
                        if(true){
                            s3Controller.download(getApplicationContext(), pict, file, new StringCallback() {
                                @Override
                                public void onComplete(String str) throws IOException {
                                    StringCallback.super.onComplete(str);

                                    putPicturePin(pict, file, annotationPlugin);


                                }
                            });
                        }else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        putPicturePin(pict, file, annotationPlugin);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });


                        }



                    }

                }
            });
        } catch (JsonProcessingException e) {
            Log.e("Error", e.toString());
            return 1;
        }
        return 0;
    }

    private void putPicturePin(PictureData pict, File finalFile, AnnotationPlugin annotationPlugin) throws IOException {
        Point point = Point.fromLngLat(pict.getLongitude(), pict.getLatitude());
        Bitmap bitmap = getBitmapFromFile(finalFile);
        Bitmap bitmapIcon = resize(bitmap, 200, 200);

        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withPoint(point).withIconImage(bitmapIcon);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
        pointAnnotationManager.create(pointAnnotationOptions);
        bitmap.recycle();
        bitmapIcon.recycle();
    }

    private Bitmap getBitmapFromFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD());

        fileInputStream.close();

        return image;
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