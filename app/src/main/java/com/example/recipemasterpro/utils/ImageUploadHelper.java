package com.example.recipemasterpro.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUploadHelper {

    public static final String IMAGE_TYPE_PROFILE = "profile_images";
    public static final String IMAGE_TYPE_COVER = "cover_images";
    public static final String IMAGE_TYPE_RECIPE = "recipe_images";

    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public interface ImageUploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(String error);
        void onProgress(int progress);
    }

    public ImageUploadHelper(Context context) {
        this.context = context;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public void uploadImage(Uri imageUri, String folderName, String userId,
                            ImageUploadCallback callback) {
        if (imageUri == null) {
            callback.onUploadFailure("No image selected");
            return;
        }

        // Create a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileExtension = getFileExtension(imageUri);
        String fileName = folderName + "/" + userId + "_" + timestamp + "." + fileExtension;

        StorageReference imageRef = storageReference.child(fileName);

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
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
        return extension != null ? extension : "jpg";
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                imageRef.delete().addOnSuccessListener(aVoid -> {
                    // Image deleted successfully
                }).addOnFailureListener(e -> {
                    // Handle error silently
                });
            } catch (Exception e) {
                // Invalid URL or other error
            }
        }
    }
}