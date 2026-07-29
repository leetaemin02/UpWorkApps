package com.example.jobsearchapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FirebaseStorageHelper {

    private final StorageReference storageReference;

    public FirebaseStorageHelper() {
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public void uploadCV(Uri fileUri, String userId, UploadCallback callback) {

        StorageReference fileRef = storageReference
                .child("cv")
                .child(userId + ".pdf");

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        callback.onSuccess(uri.toString()))
                                .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }

    public void uploadAvatar(Context context, Uri fileUri, String userId, UploadCallback callback) {
        try {
            // Nén ảnh để tăng tốc độ tải lên
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Nén ảnh xuống còn 70% chất lượng để cân bằng giữa độ nét và dung lượng
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileRef = storageReference
                    .child("avatars")
                    .child(userId + ".jpg");

            fileRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(uri ->
                                            callback.onSuccess(uri.toString()))
                                    .addOnFailureListener(callback::onFailure))
                    .addOnFailureListener(callback::onFailure);
            
            if (inputStream != null) inputStream.close();
            
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}