package com.dhananjay.myapplication.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dhananjay.myapplication.R;
import com.dhananjay.myapplication.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostUpdateActivity extends BaseActivity {

    public static final String EXTRA_POST_KEY = "post_key";
    private static final String TAG = "PostUpdateActivity";
    private static final String REQUIRED = "Required";

    private EditText mTitleField;
    private EditText mBodyField;
    private Button btnUpdate;
    private Button btnDelete;
    private DatabaseReference mPostReference;
    private ValueEventListener mPostListener;
    private boolean isFailed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_update);

        setTitle("Update Post");

        // Get post key from intent
        String mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference().child("posts").child(mPostKey);

        mTitleField = findViewById(R.id.fieldTitle);
        mBodyField = findViewById(R.id.fieldBody);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                // [START_EXCLUDE]
                if (post != null) {
                    mTitleField.setText(post.title);
                    mBodyField.setText(post.body);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostUpdateActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
    }

    private void deletePost() {
        mPostReference.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setEditingEnabled(true);
                        Toast.makeText(PostUpdateActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        setEditingEnabled(true);
                        Toast.makeText(PostUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();
        isFailed = false;

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        } else {
            mPostReference.child("title").setValue(title)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isFailed = false;
                            Toast.makeText(PostUpdateActivity.this, "Successfully changed title", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            isFailed = true;
                            Toast.makeText(PostUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        } else {
            mPostReference.child("body").setValue(body)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isFailed = false;
                            Toast.makeText(PostUpdateActivity.this, "Successfully changed body", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            isFailed = true;
                            Toast.makeText(PostUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        setEditingEnabled(true);
        if (!isFailed) {
            finish();
        }
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
    }
}
