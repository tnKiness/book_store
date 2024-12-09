package com.example.book_store;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.book_store.admin.AdminMenuActivity;
import com.example.book_store.model.User;
import com.example.book_store.sharedpreferences.Constants;
import com.example.book_store.sharedpreferences.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    EditText txtPhone,txtPass;
    Button btnLogin, btnRegister;
    private User user;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ Firebase Console
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnGoogleSignIn = findViewById(R.id.login_btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        txtPhone = (EditText) findViewById(R.id.login_txtPhone);
        txtPass = (EditText) findViewById(R.id.login_txtPass);
        btnLogin = findViewById(R.id.login_btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = txtPhone.getText().toString().trim();
                String pass = txtPass.getText().toString().trim();
                if(phone.isEmpty() || pass.length() < 6){
                    Toast.makeText(LoginActivity.this, "Trường không được để trống", Toast.LENGTH_SHORT).show();
                }else{
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("FirebaseDebug", "Snapshot: " + snapshot.toString());
                            if(snapshot.hasChild(phone)){
                                final User user = snapshot.child(phone).getValue(User.class);
                                if(user.getPassword().equals(pass)){
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                    writeToSharedPreferences(user);
                                    //is admin
                                    if(user.getIsAdmin() == 1){
                                        Intent adminMenu = new Intent(getApplicationContext(), AdminMenuActivity.class);
                                        //adminMenu.putExtra(USER_KEY,user);
                                        startActivity(adminMenu);
                                    }
                                    else{
                                        //Main menu Activity
                                        //Gui user
                                        Intent menu = new Intent(getApplicationContext(),MenuActivity.class);
                                        //menu.putExtra(USER_KEY,user);
                                        startActivity(menu);
                                    }

                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!\nVui lòng thử lại", Toast.LENGTH_LONG).show();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        });
        btnRegister = (Button) findViewById(R.id.login_btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToRegisterActivity();
            }
        });

    }
    private void writeToSharedPreferences(User user){
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext(), Constants.LOGIN_KEY_PREFERENCE_NAME);
//        sharedPreferences = getSharedPreferences("book_store", Context.MODE_PRIVATE);
//        SharedPreferences.Editor  editor = sharedPreferences.edit();
        String phone = user.getPhone();
        int isAdmin = user.getIsAdmin();
        preferenceManager.putString(Constants.LOGIN_PHONE,phone);
        preferenceManager.putInt(Constants.LOGIN_IS_ADMIN,isAdmin);
//        editor.putString("phone",phone);
//        editor.putInt("isAdmin",isAdmin);
//        editor.commit();
    }
    private void changeToRegisterActivity(){
        Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
        startActivity(intent);
    }

    private void signInWithGoogle() {
        // Đăng xuất trước khi thực hiện đăng nhập mới
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Sau khi đăng xuất thành công, tiếp tục thực hiện đăng nhập
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("Google Sign-In", "Google sign in failed", e);
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            writeToSharedPreferences(user);
                            // Chuyển hướng tới màn hình chính của người dùng
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void writeToSharedPreferences(FirebaseUser user) {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext(), Constants.LOGIN_KEY_PREFERENCE_NAME);
        String email = user.getEmail(); // lưu Email()
        String name = user.getDisplayName();
        preferenceManager.putString(Constants.LOGIN_EMAIL, email);
        preferenceManager.putString(Constants.LOGIN_NAME, name);
        preferenceManager.putInt(Constants.LOGIN_IS_ADMIN, 0);
    }
}