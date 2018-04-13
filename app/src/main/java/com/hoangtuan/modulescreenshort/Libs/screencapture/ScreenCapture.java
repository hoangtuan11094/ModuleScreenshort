package com.hoangtuan.modulescreenshort.Libs.screencapture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hoangtuan.modulescreenshort.Libs.screencapture.FileUtil.SCREENCAPTURE_PATH;

public class ScreenCapture {

    private static String TAG = ScreenCapture.class.getName();
    private AppCompatActivity mActivity;

    private final int REQUEST_CODE_SAVE_IMAGE_FILE = 110;

    private int mWindowWidth;
    private int mWindowHeight;
    private int mScreenDensity;

    private String mImageName;
    private String mImagePath;

    private VirtualDisplay mVirtualDisplay;
    private WindowManager mWindowManager;
    private ImageReader mImageReader;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    private int mResultCode;
    private Intent mResultData;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private Bitmap mBitmap;
    private boolean isSaveImageEnable = true;


    private AtomicBoolean mIsQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;

    private boolean isScreenshot = false;
    private OnCaptureListener mCaptureListener = null;

    public interface OnCaptureListener {
        void onScreenCaptureSuccess(Bitmap bitmap, String savePath);

        void onScreenCaptureFailed(String errorMsg);

    }


    public static ScreenCapture newInstance(AppCompatActivity activity) {
        return new ScreenCapture(activity);
    }
Context contextx;
    public ScreenCapture(AppCompatActivity activity) {
        this.mActivity = activity;
        contextx=activity.getApplicationContext();
        createEnvironment();
    }

    private void createEnvironment() {
        mImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                +File.separator+"Screenshots"+File.separator;
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = mWindowManager.getDefaultDisplay().getWidth();
        mWindowHeight = mWindowManager.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;
        mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, 0x1, 2);

        mMediaProjectionManager = (MediaProjectionManager) mActivity.
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);


    }

    public void screenCapture() {
        isScreenshot = true;
        if (startScreenCapture()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "start startCapture");
                    startCapture();
                }
            }, 200);
        }
    }

    private void startCapture() {
        Random random=new Random();
        int a=random.nextInt();
        if (TextUtils.isEmpty(mImageName)) {
            mImageName = System.currentTimeMillis()+a+ ".png";
        }
        Log.i(TAG, "image name is : " + mImageName);
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            Log.e(TAG, "image is null.");
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        mBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        mBitmap.copyPixelsFromBuffer(buffer);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height);
        image.close();

        stopScreenCapture();
        if (mBitmap != null) {
            Log.d(TAG, "bitmap create success");
            if (isSaveImageEnable) {
                saveToFile();
            } else {
                if (mCaptureListener != null) {
                    mCaptureListener.onScreenCaptureSuccess(mBitmap, null);
                }
            }
        } else {
            Log.d(TAG, "bitmap is null");
            if (mCaptureListener != null) {
                mCaptureListener.onScreenCaptureFailed("Get bitmap failed.");
            }
        }
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    private boolean startScreenCapture() {
        Log.d(TAG, "startScreenCapture");
        if (this == null) {
            return false;
        }
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
            return true;
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
            return true;
        } else {
            Log.d(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            mActivity.startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
            return false;
        }
    }

    private void setUpVirtualDisplay() {
        if (isScreenshot) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                    mWindowWidth, mWindowHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void saveToFile() {
        try {
            File fileFolder = new File(mImagePath);
            if (!fileFolder.exists())
                fileFolder.mkdirs();
            File file = new File(mImagePath, System.currentTimeMillis()+mImageName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            MediaScannerConnection.scanFile(contextx, new String[] { file.getPath() },
                    new String[] { "image/jpeg" }, null);
            if (mCaptureListener != null) {
                mCaptureListener.onScreenCaptureSuccess(mBitmap, file.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        isScreenshot=false;

    }
    private void release() {
        mIsQuit.set(false);
        mMuxerStarted = false;
        Log.i(TAG, " release() ");
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.w(TAG, "User cancelled.");
                return;
            }
            if (this == null) {
                return;
            }
            mResultCode = resultCode;
            mResultData = data;
            if (isScreenshot) {
                screenCapture();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SAVE_IMAGE_FILE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (isScreenshot) {
                        saveToFile();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Toast.makeText(mActivity, "Permission denied", Toast.LENGTH_SHORT).show();
                    if (mCaptureListener != null) {
                        mCaptureListener.onScreenCaptureFailed("Permission denied");
                    }
                }
                break;
            }
        }
    }

    public void cleanup() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        release();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}
