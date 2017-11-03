package com.sarl.monify;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.URI;
import java.security.Permission;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS,
                             Manifest.permission.READ_CALL_LOG};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheckContact = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int permissionCheckSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        int permissionCheckCallLog = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);

        if(permissionCheckCallLog == PackageManager.PERMISSION_DENIED ||
                permissionCheckContact == PackageManager.PERMISSION_DENIED ||
                permissionCheckSms == PackageManager.PERMISSION_DENIED){
            Log.d("MainActivity", "Getting the permission");
            ActivityCompat.requestPermissions(this, permissions, 1 );
        }else{
            Log.d("MainActivity", "Starting DataUploadService.");
            Intent intent = new Intent(this, DataUploadService.class);
            startService(intent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission Callback", "Starting DataUploadService.");
                Intent intent = new Intent(this, DataUploadService.class);
                startService(intent);
            }
        }else{
            Log.d("Permission Callback", "Requesting permission again.");
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }
}