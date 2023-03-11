package com.yancy.geolocalizacin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    Button btnObtenerUbicacion, btnEnviar, btnMapa;
    TextView tvLatitud, tvLongitud, tvDireccion;

    public static final int CODIGO_UBICACION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnObtenerUbicacion = findViewById(R.id.btnUbicacion);
        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvDireccion = findViewById(R.id.tvDireccion);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnMapa = findViewById(R.id.btnMapa);

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mapa = new Intent(MainActivity.this, Mapa.class);
                mapa.putExtra("Latitud",tvLatitud.getText().toString());
                mapa.putExtra("Longitud",tvLongitud.getText().toString());
                startActivity(mapa);

            }
        });
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hola, te adjunto mi ubicación: https://maps.google.com/?q="
                        +tvLatitud.getText().toString()+","+tvLongitud.getText().toString());
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            }

        });

        btnObtenerUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ObtenerUbicacion();
            }


            public void ObtenerUbicacion() {
                verificarPermisosUbicación();

            }
        });
    }



    public void verificarPermisosUbicación() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,}, 100);

        } else {
            iniciarUbicacion();
        }


    }



    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_UBICACION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarUbicacion();
                return;
            }
        }

    }



    public void iniciarUbicacion() {
        LocationManager objGestorUbicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion ubicador = new Localizacion();
        ubicador.setMainActivity(this);
        final boolean gpsEnabled = objGestorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,}, CODIGO_UBICACION);
            return;
        }
        objGestorUbicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, (LocationListener) ubicador);
        objGestorUbicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, (LocationListener) ubicador);
        Toast.makeText(MainActivity.this, "Localizacion Inicializada",
                Toast.LENGTH_SHORT).show();
        tvLatitud.setText("");
        tvLongitud.setText("");
        tvDireccion.setText("");
    }



    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            tvLatitud.setText(String.valueOf(loc.getLatitude()));
            tvLongitud.setText(String.valueOf(loc.getLongitude()));
            this.mainActivity.obtenerDireccion(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("EstatusGPS", "GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("EstatusGPS", "GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }

        }
    }



    public void obtenerDireccion(Location ubicacion) {
        if (ubicacion.getLatitude() != 0.0 && ubicacion.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        ubicacion.getLatitude(), ubicacion.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    tvDireccion.setText(DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}