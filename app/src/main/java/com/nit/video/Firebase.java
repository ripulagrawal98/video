package com.nit.video;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Firebase extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();



    private Button choose_video ;
    protected static RecyclerAdapter sAdapter;
    protected static RecyclerView sRecyclerView;
    protected static RecyclerView.LayoutManager sLayoutManager;

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private int REQUEST_CODE1 = 122;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        choose_video = findViewById(R.id.choosebutton);
        sRecyclerView = findViewById(R.id.upload_list);

        fileNameList = new ArrayList<String>();
        fileDoneList = new ArrayList<String>();

        sAdapter = new RecyclerAdapter(fileNameList,fileDoneList);

        sRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sRecyclerView.setHasFixedSize(true);
        sRecyclerView.setAdapter(sAdapter);


        choose_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select a Video"),REQUEST_CODE1);
            }
        });





    }

    protected void onActivityResult(int request_code, int result_code, @Nullable Intent data) {

        super.onActivityResult(request_code, result_code, data);
        if(request_code == REQUEST_CODE1)
        {
            if(result_code == RESULT_OK)
            {
                if(data.getData() != null)
                {
                   Uri uri = data.getData();
                    upload_to_firebase(uri);
                }
                else if(data.getClipData()!= null)
                {
                    for(int i =0;i<data.getClipData().getItemCount();i++)
                    {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        String fileName = getFileName(uri);
                        fileNameList.add(fileName);
                        fileDoneList.add("uploading");

                        sAdapter.notifyDataSetChanged();



                        // Code for showing progressDialog while uploading
                        final ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setTitle("Uploading...");
                        progressDialog.show();

//                        StorageReference videoref = storageRef.child("videos");
                        StorageReference video2ref = storageRef.child("videos").child(UUID.randomUUID().toString());
                        final int finalI = i;
                        UploadTask uploadTask = video2ref.putFile(uri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();

                                fileDoneList.remove(finalI);
                                fileDoneList.add(finalI, "done");

                                sAdapter.notifyDataSetChanged();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                System.out.println("Upload is " + progress + "% done");
                                progressDialog.setMessage("Uploaded " + (int)progress + "%");
                            }
                        });




                    }
                }
            }
        }
    }

protected void upload_to_firebase(Uri uri){

    // Code for showing progressDialog while uploading
   final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Uploading...");
    progressDialog.show();

    StorageReference videoref = storageRef.child("videos");
    StorageReference video2ref = videoref.child(UUID.randomUUID().toString());
    UploadTask uploadTask = video2ref.putFile(uri);
    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_SHORT).show();
        }
    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            System.out.println("Upload is " + progress + "% done");
            progressDialog.setMessage("Uploaded " + (int)progress + "%");
        }
    });



}

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


}
