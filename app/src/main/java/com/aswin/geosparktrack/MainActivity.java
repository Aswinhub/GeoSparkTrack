package com.aswin.geosparktrack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aswin.locationtracker.CurrentUser;
import com.aswin.locationtracker.RealmDB;
import com.aswin.locationtracker.UserModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "aswin";

    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SupportMapFragment mapView;
    private TextView textView;
    private GoogleMap googleMap;
    private RealmResults<CurrentUser> realmResults = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        textView = findViewById(R.id.tv);

    }

    private void SyncMap() {
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMinZoomPreference(12);

        setMapData();


    }

    private void setMapData() {
        if (realmResults != null && realmResults.size() > 0) {

            UserModel userModel = RealmDB.with(this).getUserData(realmResults.get(0).getUsername(), "");

            Log.e(TAG, "setMapData: " + userModel.getLatitude().size());
            for (int i = 0; i < userModel.getLatitude().size(); i++) {
                LatLng latLng = new LatLng(Double.parseDouble(userModel.getLatitude().get(i)),
                        Double.parseDouble(userModel.getLongitude().get(i)));

                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


            }
            textView.setVisibility(View.GONE);
        }else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        checkPermissions();


    }

    private void initializeRealm() {
        RealmDB.with(this).configureRealm();
        Realm realm = Realm.getDefaultInstance();
        realmResults = realm.where(CurrentUser.class).findAll();

        SyncMap();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, permissions, 1);

        } else
            initializeRealm();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isDenied = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "permission granted ");
                initializeRealm();
            } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Log.e(TAG, "permission denied ");
                isDenied = true;
            }

        }

        if (isDenied)
            checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
