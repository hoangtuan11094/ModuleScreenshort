package com.hoangtuan.modulescreenshort;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.hoangtuan.modulescreenshort.Adapter.ImageViewAdpater;
import com.hoangtuan.modulescreenshort.Adapter.ImageViewControlAdpater;
import com.hoangtuan.modulescreenshort.Constants.Key;

import java.io.File;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ScreenShotActivity extends AppCompatActivity {
    public static void startActivityToScreenShotActivity(Context context, String path, String fileName) {
        Intent intent = new Intent(context, ScreenShotActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Key.PATH_KEY, path);
        intent.putExtra(Key.FILENAME_KEY, fileName);
        context.startActivity(intent);
    }

    RecyclerView recyclerView, recyImagesControl;
    ImageViewAdpater imageViewAdpater;
    ImageViewControlAdpater imageViewControlAdpater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        recyclerView = (RecyclerView) findViewById(R.id.recyImages);
        recyImagesControl = (RecyclerView) findViewById(R.id.recyImagesControl);
        recyImagesControl.setHasFixedSize(true);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyImagesControl.setLayoutManager(manager1);
        imageViewAdpater = new ImageViewAdpater(this);
        imageViewControlAdpater=new ImageViewControlAdpater(this);
        recyclerView.setAdapter(imageViewAdpater);
        recyImagesControl.setAdapter(imageViewControlAdpater);


    }
}
