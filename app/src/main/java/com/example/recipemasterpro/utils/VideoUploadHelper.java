package com.example.recipemasterpro.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoUploadHelper {

    private static final String TAG = "VideoUploadHelper";
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Context context;

    public interface VideoUploadCallback {
        void onUploadSuccess(String videoUrl);
        void onUploadFailure(String error);
        void onProgress(int progress);
    }

    public VideoUploadHelper(Context context) {
        this.context = context;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public void uploadVideo(Uri videoUri, String folderName, VideoUploadCallback callback) {
        if (videoUri == null) {
            callback.onUploadFailure("No video selected");
            return;
        }

        // Create a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileExtension = getFileExtension(videoUri);
        String fileName = folderName + "/video_" + timestamp + "." + fileExtension;

        StorageReference videoRef = storageReference.child(fileName);

        UploadTask uploadTask = videoRef.putFile(videoUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                callback.onUploadSuccess(uri.toString());
            }).addOnFailureListener(e -> {
                callback.onUploadFailure("Failed to get download URL: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            callback.onUploadFailure("Upload failed: " + e.getMessage());
        });
    }

    private String getFileExtension(Uri uri) {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        return extension != null ? extension : "mp4";
    }

    public void deleteVideo(String videoUrl) {
        if (videoUrl != null && !videoUrl.isEmpty()) {
            StorageReference videoRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
            videoRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Video deleted successfully");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting video", e);
            });
        }
    }
}
