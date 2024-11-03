package com.example.assignment2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private DatabaseClass database;
    private EditText editTextAddress, editTextLongitude, editTextLatitude;
    private TextView resultTextView;
    private Button findLocationButton;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {// onCreate method
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize your views
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextLongitude = findViewById(R.id.longitude);
        editTextLatitude = findViewById(R.id.latitude);
        resultTextView = findViewById(R.id.result);
        findLocationButton = findViewById(R.id.button);

        // Initialize your database
        database = new DatabaseClass(this);
        database.getWritableDatabase();

        // Set an OnClickListener for the button to find location
        findLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findLocation();
            }
        });

        // Set up FloatingActionButtons (for add, update, delete, and display all)
        setupFloatingActionButtons();

        // Set up location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Check permissions
        checkLocationPermission();
    }

    private void findLocation() {
        String addressToFind = editTextAddress.getText().toString().trim();
        if (addressToFind.isEmpty()) {
            resultTextView.setText("Please enter an address to find.");
            return;
        }

        Cursor cursor = database.getDataByAddress(addressToFind);//

        if (cursor != null && cursor.moveToFirst()) {
            int longitudeIndex = cursor.getColumnIndex(DatabaseClass.COLUMN_LONGITUDE);
            int latitudeIndex = cursor.getColumnIndex(DatabaseClass.COLUMN_LATITUDE);

            if (longitudeIndex != -1 && latitudeIndex != -1) {
                String longitude = cursor.getString(longitudeIndex);
                String latitude = cursor.getString(latitudeIndex);
                resultTextView.setText("Longitude: " + longitude + "\nLatitude: " + latitude);
            } else {
                resultTextView.setText("Error: Column not found.");
            }
        } else {
            resultTextView.setText("Location not found.");
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void setupFloatingActionButtons() {
        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocation();
            }
        });

        findViewById(R.id.floatingActionButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation();
            }
        });

        findViewById(R.id.floatingActionButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocation();
            }
        });

        findViewById(R.id.floatingActionButton4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAllLocations();
            }
        });
    }


    private void addLocation() {
        String address = editTextAddress.getText().toString().trim();
        String longitude = editTextLongitude.getText().toString().trim();
        String latitude = editTextLatitude.getText().toString().trim();

        if (!address.isEmpty() && !longitude.isEmpty() && !latitude.isEmpty()) {
            if (isNumeric(longitude) && isNumeric(latitude)) {
                database.insertData(address, longitude, latitude);
                resultTextView.setText("Location added.");
            } else {
                resultTextView.setText("Please enter valid numeric values for longitude and latitude.");
            }
        } else {
            resultTextView.setText("Please fill in all fields.");
        }
    }

    private void updateLocation() {
        String address = editTextAddress.getText().toString().trim();
        String longitude = editTextLongitude.getText().toString().trim();
        String latitude = editTextLatitude.getText().toString().trim();

        if (!address.isEmpty() && !longitude.isEmpty() && !latitude.isEmpty()) {
            if (isNumeric(longitude) && isNumeric(latitude)) {
                database.updateData(address, longitude, latitude);
                resultTextView.setText("Location updated.");
            } else {
                resultTextView.setText("Please enter valid numeric values for longitude and latitude.");
            }
        } else {
            resultTextView.setText("Please fill in all fields.");
        }
    }

    private void deleteLocation() {
        String address = editTextAddress.getText().toString().trim();
        if (!address.isEmpty()) {
            database.deleteData(address);
            resultTextView.setText("Location deleted.");
        } else {
            resultTextView.setText("Please enter an address to delete.");
        }
    }

    private void displayAllLocations() {
        Cursor cursor = database.getAllData(); // Method to fetch all locations from the database
        StringBuilder locations = new StringBuilder();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int addressIndex = cursor.getColumnIndex(DatabaseClass.COLUMN_ADDRESS);
                int longitudeIndex = cursor.getColumnIndex(DatabaseClass.COLUMN_LONGITUDE);
                int latitudeIndex = cursor.getColumnIndex(DatabaseClass.COLUMN_LATITUDE);

                String address = cursor.getString(addressIndex);
                String longitude = cursor.getString(longitudeIndex);
                String latitude = cursor.getString(latitudeIndex);

                locations.append("Address: ").append(address)
                        .append(", Longitude: ").append(longitude)
                        .append(", Latitude: ").append(latitude)
                        .append("\n");

            } while (cursor.moveToNext());

            resultTextView.setText(locations.toString());
        } else {
            resultTextView.setText("No locations found.");
        }

        if (cursor != null) {
            cursor.close(); // Always close the cursor to avoid memory leaks
        }
    }

    // Utility method to check if a string is numeric
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            editTextLatitude.setText(String.valueOf(latitude));
                            editTextLongitude.setText(String.valueOf(longitude));
                            resultTextView.setText("Current Location: \nLatitude: " + latitude + "\nLongitude: " + longitude);
                        } else {
                            resultTextView.setText("Unable to find current location.");
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call the superclass implementation
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                resultTextView.setText("Location permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close(); // Close the database connection
        }
    }
}

