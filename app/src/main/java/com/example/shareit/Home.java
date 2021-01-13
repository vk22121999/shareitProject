package com.example.shareit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to access Location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private WifiManager.LocalOnlyHotspotReservation mReservation;
    WifiConfiguration currentConfig;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d("log" ,"Wifi Hotspot is on now");
                mReservation = reservation;
                currentConfig = mReservation.getWifiConfiguration();

                Log.v("network -"+ currentConfig.SSID,"THE PASSWORD IS: "
                        + currentConfig.preSharedKey
                        + " \n SSID is : "
                        + currentConfig.SSID);

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("log","onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d("log", "onFailed: ");
            }
        }, new Handler());
    }

    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }


    private void scanWifi()
    {

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess(manager);
                } else {
                    // scan failure handling
                    scanFailure(manager);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = manager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure(manager);
        }

    }
    private void scanSuccess(WifiManager manager) {
       // WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = manager.getScanResults();
        Log.d("success-scan", String.valueOf(results.size()));
        for (int i = 0; i < results.size(); i++) {
            Log.d("success-scan",results.get(i).SSID);
        }
    }

    private void scanFailure(WifiManager manager) {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        //WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = manager.getScanResults();

          Log.d("failure-scan",String.valueOf(results.size()));
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void send(View view) {

        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        if(message.length()==0)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Enter a name before sending")
                    .setPositiveButton("Yes", null).show();
        }
        else

        {

            System.out.println(" sending name: "+message);

            scanWifi();


        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void recieve(View view) {

        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        if(message.length()==0)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Enter a name before recieving")
                    .setPositiveButton("Yes", null).show();
        }
        else
        {
            System.out.println(" recieving name: "+message);
            turnOnHotspot();
        }
    }

}