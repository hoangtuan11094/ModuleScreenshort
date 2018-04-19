package com.hoangtuan.modulescreenshort;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.hoangtuan.modulescreenshort.Libs.screencapture.FloatWindowsService;
import com.hoangtuan.modulescreenshort.Libs.screencapture.ScreenCapture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private ScreenCapture mScreenCapture;
    SwitchCompat swFloat, swCombo, swLac;
    SensorManager sensorManager;
    Sensor camBienRung;
    float lastX, lastY, lastZ;
    long lastTime = 0;
    NotificationManager notificationManager;
    CheckBox cbStartStop;
    public static final int REQUEST_MEDIA_PROJECTION = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        isServiceRunning();
        onEvent();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startCapture() {
        if (mScreenCapture != null) {
            mScreenCapture.screenCapture();
        }
    }


    private void onEvent() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (mScreenCapture == null)
                mScreenCapture = new ScreenCapture(this);
        }
            cbStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                        camBienRung = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        cbStartStop.setText("Dừng lại...!");
                        startSevice();
                        if (swFloat.isChecked()){
                            requestCapturePermission();
                        }else {
                            stopService(new Intent(getApplicationContext(), FloatWindowsService.class));
                        }
                        if (swLac.isChecked()){

                            Toast.makeText(MainActivity.this, "Đã chọn chế độ rung để chụp hình", Toast.LENGTH_SHORT).show();
                            if (camBienRung == null) {
                                Toast.makeText(getApplicationContext(), "Máy của bạn không có cảm biến gia tốc", Toast.LENGTH_SHORT).show();
                            } else {

                                sensorManager.registerListener(MainActivity.this, camBienRung, SensorManager.SENSOR_DELAY_NORMAL);
                            }
                        }
                        else {
                            sensorManager.unregisterListener(MainActivity.this, camBienRung);
                        }
                    }else {
                        stopSevice();
                        cbStartStop.setText("Bắt đầu...!");
                        sensorManager.unregisterListener(MainActivity.this, camBienRung);
                        stopService(new Intent(getApplicationContext(), FloatWindowsService.class));
                    }
                }
            });



    }

    private void init() {

        cbStartStop=(CheckBox)findViewById(R.id.cbStart);
        swCombo = (SwitchCompat) findViewById(R.id.swCombo);
        swFloat = (SwitchCompat) findViewById(R.id.swFloat);
        swLac = (SwitchCompat) findViewById(R.id.swLac);
    }

    private void openScreenshot() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service != null) {
                if ((getPackageName() + "." + ScreenShotSevice.class.getSimpleName()).equals(service.service.getClassName())) {
//                    serviceStatus.setText("ScreenShotService is running - " + true);
                    return true;
                }
            }
        }
//        serviceStatus.setText("ScreenShotService is running - " + false);
        return false;
    }


    public void startSevice() {

        startService(new Intent(this, ScreenShotSevice.class));
        DisplayNotification();
        Toast.makeText(this, "ScreenShotService started", Toast.LENGTH_SHORT).show();
        isServiceRunning();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScreenCapture != null) {
            mScreenCapture.cleanup();
            mScreenCapture = null;
        }
    }

    public void stopSevice() {
        stopService(new Intent(this, ScreenShotSevice.class));
        cancelNotification();
        Toast.makeText(this, "ScreenShotService stopped", Toast.LENGTH_SHORT).show();
        isServiceRunning();
    }

    public void DisplayNotification() {

// Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

//icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.mipmap.ic_launcher);

// This intent is fired when notification is clicked
        Intent tapIntent = new Intent(this, MainActivity.class);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, tapIntent, 0);

// Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        builder.setOngoing(true); // Cant cancel your notification (except NotificationManger.cancel(); )

// Content title, which appears in large type at the top of the notification
        builder.setContentTitle("Screenshot");

// Content text, which appears in smaller text below the title
        builder.setContentText("App is runing....");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }

    public void cancelNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1); // Notification ID to cancel
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        camBienRung = event.sensor;
        if (camBienRung.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();


            if ((currentTime - lastTime) > 100) {
                long diffTime = currentTime - lastTime;
                lastTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 100;

                if (speed > 5) {
//

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startCapture();
                    }
                }



                }
                lastX = x;
                lastY = y;
                lastZ = z;

            }
        }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onOpen(View view) {
        openScreenshot();
    }

    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:

                if (resultCode == RESULT_OK && data != null) {
                    FloatWindowsService.setResultData(data);
                    startService(new Intent(getApplicationContext(), FloatWindowsService.class));
                }
                break;
            case 1:
                if (mScreenCapture != null) {
                    mScreenCapture.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }

    }
}
