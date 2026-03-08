package com.example.recipemasterpro.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.recipemasterpro.R;

public class ImageLoadingHelper {

    public static void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(imageView);
    }

    public static void loadProfileImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_chef_placeholder)
                        .error(R.drawable.ic_chef_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                .into(imageView);
    }

    public static void loadThumbnail(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_video_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(imageView);
    }

    public static void clearCache(Context context) {
        new Thread(() -> {
            Glide.get(context).clearDiskCache();
        }).start();
        Glide.get(context).clearMemory();
    }
}
