package com.example.uasmaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends FragmentActivity {

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final double OFFICE_LATITUDE = -3.295556; // Latitude kantor
    private static final double OFFICE_LONGITUDE = 114.583111; // Longitude kantor
    private static final float RADIUS = 100f; // Radius dalam meter



    private MapView mapView;
    private String username;
    private Handler handler;
    private Button presenceButton;
    private  TextView absenPulang;
    private TextView absenDatang;
    private TextView statusDatang;
    private TextView statusPulang;
    private Button daftarKaryawan;

    private FloatingActionButton centerLocationButton;
    private TextView welcomeText;
    private MyLocationNewOverlay myLocationOverlay;
    private TextView timeTextView;
    private DatabaseHelper dbHelper;
    private boolean isCheckInRecorded = false;
    private static final String START_WORK_TIME = "08:00";
//    private static final String START_WORK_TIME = "00:00";
    private static final String LATE_TIME = "08:30";
//    private static final String END_WORK_TIME = "10:00";
    private static final String END_WORK_TIME = "16:00";
    private static final String END_LATE_TIME = "18:00";
    private static final String NO_CHECK_IN_AFTER = "12:00";
//    private static final String NO_CHECK_IN_AFTER = "23:50";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_dashboard);
        centerLocationButton = findViewById(R.id.centerLocationButton);
        // Inisialisasi DatabaseHelper
        dbHelper = new DatabaseHelper(this);


        welcomeText = findViewById(R.id.welcomeText);
        statusDatang = findViewById(R.id.statusDatang);
        statusPulang = findViewById(R.id.statusPulang);
        presenceButton = findViewById(R.id.presenceButton);
        absenPulang = findViewById(R.id.absenPulang);
        absenDatang = findViewById(R.id.absenDatang);
        daftarKaryawan = findViewById(R.id.daftarKaryawan);


        // intent dari login
        username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            welcomeText.setText("Hai, " + username);
        }
        if (!TextUtils.isEmpty(statusDatang.getText()) || !TextUtils.isEmpty(statusPulang.getText())) {
            loadPresenceData();
        }


        mapView = findViewById(R.id.mapView);
        // Inisialisasi TextView
        timeTextView = findViewById(R.id.timeTextView);

        // Buat objek Handler untuk memperbarui waktu setiap detik
        handler = new Handler();

        // Jalankan metode updateCurrentTime untuk pertama kali
        updateCurrentTime();

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        setupMapView();


        if (!isLocationEnabled()) {
            // Menampilkan dialog pengaturan lokasi jika lokasi tidak diaktifkan
            showLocationSettingsDialog();
        } else {
            // Memeriksa izin lokasi
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Meminta izin lokasi jika belum diberikan
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                // Lakukan sesuatu dengan lokasi karena izin sudah diberikan
                showUserLocation();
            }
        }


        daftarKaryawan.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), TampilDataActivity.class);
                startActivity(intent);
        });
        presenceButton.setOnClickListener(v -> checkPresence());

        // Tambahkan listener untuk tombol pusatkan lagi lokasi
        centerLocationButton.setOnClickListener(v -> centerUserLocation());
    }

    private void updateCurrentTime() {
        // Buat Runnable untuk memperbarui waktu setiap detik
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Dapatkan waktu saat ini
                long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, HH:mm:ss", new Locale("id", "ID"));
                String currentTimeString = sdf.format(new Date(currentTimeMillis));

                // Tampilkan waktu saat ini pada TextView
                timeTextView.setText(currentTimeString);

                // Jalankan metode ini lagi setelah 1 detik
                handler.postDelayed(this, 1000);
            }
        };

        // Jalankan Runnable untuk pertama kali
        updateTimeRunnable.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hentikan handler saat Activity dihancurkan
        handler.removeCallbacksAndMessages(null);
    }
    // Metode yang dipanggil saat tombol diklik
    private void centerUserLocation() {
        setupMapView();

        // Tampilkan toast "Loading"
        Toast.makeText(this, "Loading, please wait...", Toast.LENGTH_SHORT).show();

        // Handler untuk mensimulasikan proses loading (ganti dengan logika pengambilan lokasi yang sebenarnya jika tersedia)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (myLocationOverlay.isMyLocationEnabled()) {
                    GeoPoint userLocation = myLocationOverlay.getMyLocation();
                    if (userLocation != null) {
                        mapView.getController().setZoom(18); // Sesuaikan level zoom jika diperlukan
                        mapView.getController().setCenter(userLocation);
                        Toast.makeText(DashboardActivity.this, "Peta dipusatkan ke lokasi Anda: " + userLocation.getLatitude() + ", " + userLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DashboardActivity.this, "Jaringan anda bermasalah!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Fitur lokasi belum diaktifkan", Toast.LENGTH_SHORT).show();
                }
            }
        }, 3000); // Simulasi delay loading (3 detik). Sesuaikan jika perlu.
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aktifkan Lokasi")
                .setMessage("Layanan lokasi tidak aktif. Aktifkan lokasi untuk menggunakan fitur ini.")
                .setPositiveButton("Pengaturan", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(DashboardActivity.this, "Lokasi tidak aktif. Fitur tidak dapat digunakan.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showUserLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setupMapView() {
        // Pengaturan tile source untuk OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        // Tambahkan overlay untuk kantor
        GeoPoint startPoint = new GeoPoint(OFFICE_LATITUDE, OFFICE_LONGITUDE);
        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setTitle("Kantor");
        mapView.getOverlays().add(marker);

        // Menambahkan lingkaran di sekitar kantor
        Polygon circle = createCircle(startPoint, RADIUS);
        mapView.getOverlayManager().add(circle);

        // Aktifkan fitur lokasi pada peta
        myLocationOverlay.enableMyLocation();
        mapView.getOverlayManager().add(myLocationOverlay);

        // Tambahkan overlay kompas
        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlayManager().add(compassOverlay);

        // Pusatkan ke lokasi pengguna
        if (myLocationOverlay.isMyLocationEnabled()) {
            GeoPoint userLocation = myLocationOverlay.getMyLocation();
            if (userLocation != null) {
                mapView.getController().setZoom(30); // Sesuaikan level zoom jika diperlukan
                mapView.getController().setCenter(userLocation);
            } else {
                // Lokasi belum tersedia, pusatkan ke kantor
                mapView.getController().setZoom(30); // Sesuaikan level zoom jika diperlukan
                mapView.getController().setCenter(startPoint);
            }
        } else {
            // Lokasi belum diaktifkan, pusatkan ke kantor
            mapView.getController().setZoom(30); // Sesuaikan level zoom jika diperlukan
            mapView.getController().setCenter(startPoint);
        }
    }


    private void showUserLocation() {
        // Pastikan fitur lokasi telah diaktifkan
        if (myLocationOverlay.isMyLocationEnabled()) {
            myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
                // Dapatkan lokasi pengguna
                GeoPoint userLocation = myLocationOverlay.getMyLocation();
                // Pastikan lokasi pengguna tidak null
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (userLocation != null) {
                        double latitude = userLocation.getLatitude();
                        double longitude = userLocation.getLongitude();

                        // Perbesar peta ke lokasi pengguna
                        mapView.getController().setZoom(20); // Sesuaikan level zoom jika diperlukan
                        mapView.getController().setCenter(userLocation);

                        // Tampilkan pesan selamat datang dengan koordinat lokasi pengguna
                        Toast.makeText(this, "Selamat datang! Lokasi Anda: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Tidak dapat menemukan lokasi Anda, periksa jaringan anda!", Toast.LENGTH_SHORT).show();
                    }
                    }, 3000);
            }));
        } else {
            Toast.makeText(this, "Fitur lokasi belum diaktifkan", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPresence() {
        // Dapatkan waktu saat ini
        String currentTime = getCurrentTime();
        
        // Dapatkan lokasi pengguna
        GeoPoint userLocation = myLocationOverlay.getMyLocation();
        if (userLocation == null) {
            Toast.makeText(this, "Tidak dapat menemukan lokasi Anda", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint userPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());

        // Hitung jarak antara lokasi pengguna dan kantor
        float[] results = new float[1];
        Location.distanceBetween(userPoint.getLatitude(), userPoint.getLongitude(), OFFICE_LATITUDE, OFFICE_LONGITUDE, results);
        float distanceInMeters = results[0];

        // Jika pengguna berada dalam radius yang ditentukan, izinkan presensi
        if (distanceInMeters <= RADIUS) {
            if (isCheckInRecorded) {
                // Jika sudah ada catatan absen datang, rekam absen pulang
                if (isBeforeTime(currentTime, END_WORK_TIME)) {
                    Toast.makeText(this, "Anda belum bisa absen pulang sebelum jam 16:00", Toast.LENGTH_SHORT).show();
                } else {
                    recordCheckOut();
                }
            } else {
                if (isBeforeTime(currentTime, START_WORK_TIME)){
                    Toast.makeText(DashboardActivity.this, "Absen dibuka jam 08:00", Toast.LENGTH_SHORT).show();
                }
                // Jika belum ada catatan absen datang, rekam absen datang
                else if (isAfterTime(currentTime, NO_CHECK_IN_AFTER)) {
                    Toast.makeText(DashboardActivity.this, "Absen ditutup setelah jam 12:00", Toast.LENGTH_SHORT).show();
                    presenceButton.setEnabled(false);
                    presenceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.buttontidak));
                } else {
                    recordCheckIn();
                    Toast.makeText(DashboardActivity.this, "Presensi Berhasil", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            Toast.makeText(DashboardActivity.this, "Anda tidak dalam jangkauan", Toast.LENGTH_SHORT).show();
        }
    }

    private void recordCheckIn() {
        String waktuCekIn = getCurrentTime();
        String statusCekIn = getCheckInStatus(waktuCekIn);

        absenDatang.setVisibility(View.VISIBLE);
        absenDatang.setText("Absen datang pada: " + waktuCekIn);
        statusDatang.setText(statusCekIn);
        if (statusCekIn.equals("Terlambat"))
        {
         statusDatang.setTextColor(ContextCompat.getColor(this, R.color.merah));
        } else if (statusCekIn.equals("Hadir")) {
            statusDatang.setTextColor(ContextCompat.getColor(this, R.color.hijau));
        }
        statusDatang.setVisibility(View.VISIBLE);

        isCheckInRecorded = true;
        presenceButton.setText("Absen Pulang");

        String tanggal = getCurrentDate();
        dbHelper.insertDataKaryawan(this, username, waktuCekIn, statusCekIn, tanggal);
    }

    private void recordCheckOut() {
        String waktuCekOut = getCurrentTime();
        String statusCekOut = getCheckOutStatus(waktuCekOut);

        absenPulang.setVisibility(View.VISIBLE);
        absenPulang.setText("Absen pulang pada: " + waktuCekOut);
        statusPulang.setText(statusCekOut);
        if (statusCekOut.equals("Terlambat"))
        {
            statusPulang.setTextColor(ContextCompat.getColor(this, R.color.merah));
        } else if (statusCekOut.equals("Hadir")) {
            statusPulang.setTextColor(ContextCompat.getColor(this, R.color.hijau));
        }
        statusPulang.setVisibility(View.VISIBLE);

        presenceButton.setText("Anda telah melakukan presensi");
        presenceButton.setEnabled(false);
        presenceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.buttontidak));

        isCheckInRecorded = false;

        String tanggal = getCurrentDate();
        dbHelper.updateDataKaryawan(username, waktuCekOut, statusCekOut, tanggal);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCheckInStatus(String currentTime) {
        return getStatus(currentTime, START_WORK_TIME, LATE_TIME);
    }

    private String getCheckOutStatus(String currentTime) {
        return getStatus(currentTime, END_WORK_TIME, END_LATE_TIME);
    }

    private String getStatus(String currentTime, String startTime, String lateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date current = sdf.parse(currentTime);
            Date start = sdf.parse(startTime);
            Date late = sdf.parse(lateTime);

            if (current.before(start)) {
                return "Belum jam kerja";
            } else if (current.before(late)) {
                return "Hadir";
            } else {
                return "Terlambat";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private boolean isBeforeTime(String currentTime, String comparisonTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date current = sdf.parse(currentTime);
            Date comparison = sdf.parse(comparisonTime);

            return current.before(comparison);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAfterTime(String currentTime, String comparisonTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date current = sdf.parse(currentTime);
            Date comparison = sdf.parse(comparisonTime);

            return current.after(comparison);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Polygon createCircle(GeoPoint center, float radius) {
        List<GeoPoint> points = new ArrayList<>();
        int numPoints = 100;
        double radiusDegrees = radius / (111 * 1000); // Convert meters to decimal degrees

        for (int i = 0; i < numPoints; i++) {
            double theta = ((double) i / (double) numPoints) * (2 * Math.PI);
            double x = center.getLatitude() + radiusDegrees * Math.cos(theta);
            double y = center.getLongitude() + radiusDegrees * Math.sin(theta);
            points.add(new GeoPoint(x, y));
        }

        Polygon circle = new Polygon(mapView);
        circle.setPoints(points);
        circle.setFillColor(0x1500FF00); // Set the circle's fill color (semi-transparent green)
        circle.setStrokeColor(0xFF000000); // Set the circle's outline color (black)
        circle.setStrokeWidth(2); // Set the circle's outline width
        return circle;
    }

    //jika user melakukan login lagi maka akan ada save an nya
    private void loadPresenceData() {
        String currentDate = getCurrentDate(); // Implement this method to get current date in suitable format
        Cursor cursor = dbHelper.getLastPresenceData(username, currentDate);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Columns indexes based on your KARYAWAN_TABLE schema
                    int checkinTimeIndex = cursor.getColumnIndexOrThrow("waktuCekIn");
                    int checkinStatusIndex = cursor.getColumnIndexOrThrow("statusCekIn");
                    int checkoutTimeIndex = cursor.getColumnIndexOrThrow("waktuCekOut");
                    int checkoutStatusIndex = cursor.getColumnIndexOrThrow("statusCekOut");

                    String checkinTime = cursor.getString(checkinTimeIndex);
                    String checkinStatus = cursor.getString(checkinStatusIndex);
                    String checkoutTime = cursor.getString(checkoutTimeIndex);
                    String checkoutStatus = cursor.getString(checkoutStatusIndex);

                    if (checkinTime != null) {
                        absenDatang.setVisibility(View.VISIBLE);
                        absenDatang.setText("Absen datang pada: " + checkinTime);
                        statusDatang.setText(checkinStatus);
                        statusDatang.setVisibility(View.VISIBLE);
                        if (checkinStatus.equals("Terlambat")) {
                            statusDatang.setTextColor(ContextCompat.getColor(this, R.color.merah));
                        } else if (checkinStatus.equals("Hadir")) {
                            statusDatang.setTextColor(ContextCompat.getColor(this, R.color.hijau));
                        }
                        isCheckInRecorded = true;
                        presenceButton.setText("Absen Pulang");

                        if (checkoutTime != null) {
                            absenPulang.setVisibility(View.VISIBLE);
                            absenPulang.setText("Absen pulang pada: " + checkoutTime);
                            statusPulang.setText(checkoutStatus);
                            statusPulang.setVisibility(View.VISIBLE);

                            if (checkoutStatus.equals("Terlambat")) {
                                statusPulang.setTextColor(ContextCompat.getColor(this, R.color.merah));
                            } else if (checkoutStatus.equals("Hadir")) {
                                statusPulang.setTextColor(ContextCompat.getColor(this, R.color.hijau));
                            }
                            presenceButton.setBackground(ContextCompat.getDrawable(this, R.drawable.buttontidak));
                            presenceButton.setText("Anda telah melakukan presensi");
                            presenceButton.setEnabled(false);
                            isCheckInRecorded = false;
                        }
                    }
                } else {
                    // No data found for the current user and date
                    Toast.makeText(this, "No presence data found for today.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading presence data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        } else {
            // Cursor is null, indicating no data or an error in the query
            Toast.makeText(this, "No presence data found for today.", Toast.LENGTH_SHORT).show();
        }
    }

}
