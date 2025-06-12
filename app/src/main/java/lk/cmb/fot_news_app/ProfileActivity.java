package lk.cmb.fot_news_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, emailTextView;
    private Button editInfoButton, signOutButton;
    private CircleImageView profileImageView;
    private String userId; // username

    private Uri selectedImageUri = null; // Holds the locally chosen image

    // New: Image picker launcher
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- Back button logic ---
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Initialize views
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        editInfoButton = findViewById(R.id.edit_info_btn);
        signOutButton = findViewById(R.id.sign_out_btn);
        profileImageView = findViewById(R.id.profile_image);

        // Image picker logic
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            profileImageView.setImageURI(imageUri);
                        }
                    }
                });

        // Handle image click
        profileImageView.setOnClickListener(v -> openImagePicker());

        // Get username from SharedPreferences (preferred way!)
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getString("username", null);
        if (userId == null) {
            // fallback to Intent if needed (legacy)
            userId = getIntent().getStringExtra("username");
        }
        fetchUserData(userId);

        // Edit Info button click listener
        editInfoButton.setOnClickListener(v -> {
            showEditProfileDialog(
                    usernameTextView.getText().toString(),
                    emailTextView.getText().toString()
            );
        });

        // Sign out button click listener
        signOutButton.setOnClickListener(v -> signOut());
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void fetchUserData(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "No user specified!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usernameTextView.setText(userId);

                    String email = dataSnapshot.child("email").getValue(String.class);
                    emailTextView.setText(email);

                    // Only load from database if user has not picked an image this session
                    if (selectedImageUri == null) {
                        String imageUrl = dataSnapshot.child("image").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.noprofilepic)
                                    .error(R.drawable.noprofilepic)
                                    .into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.noprofilepic);
                        }
                    } // else: show picked image
                } else {
                    Toast.makeText(ProfileActivity.this, "No data found for the user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ----- SHOW EDIT PROFILE DIALOG -----
    private void showEditProfileDialog(String currentUsername, String currentEmail) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        EditText editUsername = dialogView.findViewById(R.id.editUsername);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);

        editUsername.setText(currentUsername);
        editEmail.setText(currentEmail);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(v -> {
            String newUsername = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            if (newUsername.isEmpty()) {
                editUsername.setError("Username required");
                return;
            }
            if (newEmail.isEmpty()) {
                editEmail.setError("E-mail required");
                return;
            }

            // --- USERNAME UPDATE LOGIC ---
            if (!newUsername.equals(userId)) {
                updateUsernameInFirebase(userId, newUsername, newEmail, dialog);
            } else {
                // Username not changed, just update email
                updateUserProfileInFirebase(userId, newEmail, dialog);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // ---- Move the dialog upwards! ----
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.y = -650; // Negative value moves it up; adjust as needed!
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    // ---- UPDATE ONLY EMAIL ----
    private void updateUserProfileInFirebase(String username, String newEmail, AlertDialog dialog) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        userRef.child("email").setValue(newEmail)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    emailTextView.setText(newEmail);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ---- CHANGE USERNAME (move node!) ----
    private void updateUsernameInFirebase(String oldUsername, String newUsername, String newEmail, AlertDialog dialog) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference oldRef = usersRef.child(oldUsername);
        DatabaseReference newRef = usersRef.child(newUsername);

        oldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "Old user not found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if new username already exists
                newRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot newSnap) {
                        if (newSnap.exists()) {
                            Toast.makeText(ProfileActivity.this, "Username already taken!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Copy data to new username node
                            newRef.setValue(snapshot.getValue())
                                    .addOnSuccessListener(aVoid -> {
                                        // Update email in new node
                                        newRef.child("email").setValue(newEmail);

                                        // Remove old username node
                                        oldRef.removeValue()
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Toast.makeText(ProfileActivity.this, "Username changed successfully!", Toast.LENGTH_SHORT).show();

                                                    // Update UI and internal userId
                                                    userId = newUsername;
                                                    usernameTextView.setText(newUsername);
                                                    emailTextView.setText(newEmail);

                                                    // ------ Save new username in SharedPreferences ------
                                                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                                            .edit()
                                                            .putString("username", newUsername)
                                                            .apply();

                                                    dialog.dismiss();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(ProfileActivity.this, "Failed to remove old user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProfileActivity.this, "Failed to copy data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void signOut() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_signout_confirm, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnYes = dialogView.findViewById(R.id.btnSignOutYes);
        Button btnCancel = dialogView.findViewById(R.id.btnSignOutCancel);

        btnYes.setOnClickListener(v -> {
            Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
            // Optional: clear SharedPreferences username on sign out
            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    .edit()
                    .remove("username")
                    .apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Move dialog upwards (optional, for visual match)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.y = -650; // Tune as needed!
            dialog.getWindow().setAttributes(layoutParams);
        }
    }
}
