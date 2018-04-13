package com.hoangtuan.modulescreenshort.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hoangtuan.modulescreenshort.Helper.BitmapHelper;
import com.hoangtuan.modulescreenshort.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by atbic on 12/4/2018.
 */

public class ImageViewAdpater extends RecyclerView.Adapter<ImageViewAdpater.ImageViewHolder> {
    Context context;
    ArrayList<String> filenames;
    File[] allFiles;

    public ImageViewAdpater(Context context) {
        this.context = context;
        filenames = new ArrayList<>();

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator + "Screenshots" + File.separator);
        if (folder.exists()) {
            allFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                }
            });
            for (File oneFile : allFiles) {
                filenames.add(oneFile.getAbsolutePath());
            }
            Collections.reverse(filenames);
        }


    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
//        Bitmap bitmap = BitmapHelper.decodeBitmapFromFile(filenames.get(position),200,600);
//        holder.imgItem.setImageBitmap(bitmap);

        Glide.with(context).load(filenames.get(position)).into(holder.imgItem);
    }

    @Override
    public int getItemCount() {
        return filenames.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imgItem = (ImageView) itemView.findViewById(R.id.imgItem);
        }
    }
}
