package com.example.jobsearchapp.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
}