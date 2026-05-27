package com.example.sosalertapp;  // ✅ Fixed package name to match file path

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accelerationValue;
    private float lastAccelerationValue;
    private static final float SHAKE_THRESHOLD = 12.0f; // ✅ Made final (resolves warning)
    private boolean isFirstSensorEvent = true;         // Prevents false trigger on first reading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        // Ensure activity_main.xml exists with a Button id="sosButton"

        // Setup accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Manual SOS button
        findViewById(R.id.sosButton).setOnClickListener(v -> checkAndSendSms());

        // Request SMS permission at start
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        }
    }

    /**
     * Checks SMS permission and sends the alert if granted.
     */
    private void checkAndSendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            sendEmergencySMS();
        } else {
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        }
    }

    private void sendEmergencySMS() {
        // 1. Define your contact list (Add as many as you need here)
        String[] emergencyContacts = {
                "03007472506", // Contact 1
                "03079066144", // Contact 2
                "03066252358"  // Contact 3
        };

        String msg = "\"I have tested it, and my project works efficiently.";

        try {
            SmsManager smsManager = SmsManager.getDefault();

            // 2. The loop that sends to ALL contacts at the same time
            for (String phoneNum : emergencyContacts) {
                if (!phoneNum.isEmpty()) {
                    smsManager.sendTextMessage(phoneNum, null, msg, null, null);
                }
            }

            Toast.makeText(this, "SOS Sent to " + emergencyContacts.length + " contacts!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // This catches errors like 'No SIM card' or 'No Balance'
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SMS. Check your SIM card.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAccelerationValue = accelerationValue;
        accelerationValue = (float) Math.sqrt(x * x + y * y + z * z);

        // Skip first event to avoid false trigger
        if (isFirstSensorEvent) {
            isFirstSensorEvent = false;
            return;
        }

        float delta = accelerationValue - lastAccelerationValue;
        if (delta > SHAKE_THRESHOLD) {
            checkAndSendSms();   // Respect permission before sending
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied – cannot send alerts", Toast.LENGTH_LONG).show();
            }
        }
    }
}