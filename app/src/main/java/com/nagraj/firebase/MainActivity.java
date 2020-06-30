package com.nagraj.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
    public static final String FIREBASE_STORAGE_DPS = "dps";
    public static final String FIREBASE_DATABASE_DPS = "dps";
    TextView tvDpName;
    ImageView ivCur, ivChange;
    Button btnChange, btnUploadDp;
    StorageReference storageReference, uploadedFileRef;
    DatabaseReference databaseReference;
    Uri changeImgUri;
    UploadTask uploadTask;
    String username, url;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser curUser;

    @Override
    protected void onStart() {
        super.onStart();
        curUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
        storageReference = FirebaseStorage.getInstance().getReference(FIREBASE_STORAGE_DPS);
        databaseReference = FirebaseDatabase.getInstance().getReference(FIREBASE_DATABASE_DPS);
        tvDpName = findViewById(R.id.tv_dp_name);
        ivCur = findViewById(R.id.iv_cur_img);
        ivChange = findViewById(R.id.iv_upload_img);
        btnChange = findViewById(R.id.btn_change_dp);
        btnUploadDp = findViewById(R.id.btn_upload_dp);

        btnChange.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 10);

        });
        btnUploadDp.setOnClickListener(v -> uploadImage());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            changeImgUri = data.getData();
            tvDpName.setText(changeImgUri.getLastPathSegment());
            Picasso.get().load(changeImgUri).into(ivChange);
        }
    }

    public void uploadImage() {
        if (changeImgUri != null) {
            if (storageReference != null) {
                username = System.currentTimeMillis() + "";
                uploadedFileRef = storageReference.child(username);
                uploadTask = uploadedFileRef.putFile(changeImgUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    showToast("File uploaded successfully!!");
                    saveFileUrlInDataBase();
                }).addOnFailureListener(e -> showToast("File not uploaded!!"))
                        .addOnProgressListener(taskSnapshot -> {
                            showToast("File is uploading !!");
                            //100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();
                        });
            }
        } else {
            showToast("Please select a image !!");
        }
    }


    public void saveFileUrlInDataBase() {
        final Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return uploadedFileRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String path = databaseReference.push().getKey();
                databaseReference.child(path).setValue(new Upload("nagraj0308", task.getResult().toString()));
                downloadPhoto();
            }
        });

    }

    public void downloadPhoto() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Upload upload = child.getValue(Upload.class);
                    if (upload.username.equals(username)) {
                        url = upload.url;
                        Picasso.get().load(url).into(ivCur);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        log("signInAnonymously:success");
                        curUser = mAuth.getCurrentUser();
                    } else {
                        log("signInAnonymously:failure" + task.getException());
                        curUser = null;
                    }
                });
    }

    public void log(String msg) {
        Log.v("NAGRAJ", msg);
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}