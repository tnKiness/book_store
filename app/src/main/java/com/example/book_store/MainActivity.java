package com.example.book_store;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.book_store.admin.AdminMenuActivity;
import com.example.book_store.sharedpreferences.Constants;
import com.example.book_store.sharedpreferences.PreferenceManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static int TIME_OUT = 4000; //Time to launch the another activity
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);

        // Khởi tạo PreferenceManager để kiểm tra trạng thái đăng nhập
        preferenceManager = new PreferenceManager(getApplicationContext(), Constants.LOGIN_KEY_PREFERENCE_NAME);

        // Chờ đợi trong một khoảng thời gian để kiểm tra trạng thái và chuyển activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Kiểm tra trạng thái đăng nhập
                String phone = preferenceManager.getString(Constants.LOGIN_PHONE);
                int isAdmin = preferenceManager.getInt(Constants.LOGIN_IS_ADMIN);

                Intent i = new Intent(MainActivity.this, MenuActivity.class); // Mặc định chuyển tới MenuActivity

                // Nếu người dùng đã đăng nhập và là admin
                if (phone != null) {
                    if (isAdmin == 1) {
                        i = new Intent(MainActivity.this, AdminMenuActivity.class); // Chuyển đến AdminMenuActivity nếu là admin
                    }
                } else {
                    // Nếu chưa đăng nhập, xóa dữ liệu trong preferences và chuyển đến MenuActivity
                    preferenceManager.clear();
                    i = new Intent(MainActivity.this, MenuActivity.class);
                }

                startActivity(i); // Bắt đầu Activity tương ứng
                finish(); // Kết thúc MainActivity
            }
        }, TIME_OUT); // Thời gian chờ là 4 giây
    }
}
