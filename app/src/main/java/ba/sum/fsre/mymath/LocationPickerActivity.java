package ba.sum.fsre.mymath;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static final String SELECTED_LOCATION = "selected_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        // Obtain the SupportMapFragment and get notified when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set a default location (example: Sarajevo)
        LatLng defaultLocation = new LatLng(43.8563, 18.4131); // Example coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Allow user to pick a location
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();  // Clear the previous marker
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

            // Once the location is selected, confirm it by clicking the button
            findViewById(R.id.confirmLocationBtn).setOnClickListener(view -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(SELECTED_LOCATION, latLng);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
    }
}
