package com.example.book_store.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.book_store.AccountFragment;
import com.example.book_store.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminMenuActivity extends AppCompatActivity {
    // Khai báo BottomNavigationView và các Fragment
    private BottomNavigationView bottomNavigationView;
    private final CRUDFragment crudFragment = new CRUDFragment();
    private final CategoryFragment categoryFragment = new CategoryFragment();
    private final OrderListFragment orderListFragment = new OrderListFragment();
    private final AdminListbook adminListbook = new AdminListbook();
    private TextView btnAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        // Ánh xạ các view từ layout
        bottomNavigationView = findViewById(R.id.admin_menu_nav);
        btnAcc = findViewById(R.id.admin_menu_btnAccount);

        // Hiển thị Fragment mặc định
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_menu_container, crudFragment)
                .commit();

        // Cài đặt sự kiện chọn item cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Xác định Fragment cần hiển thị (sử dụng if-else thay vì switch-case)
                int itemId = item.getItemId();
                if (itemId == R.id.crud) {
                    selectedFragment = crudFragment;
                } else if (itemId == R.id.category) {
                    selectedFragment = categoryFragment;
                } else if (itemId == R.id.order) {
                    selectedFragment = orderListFragment;
                } else if (itemId == R.id.list_book) {
                    selectedFragment = adminListbook;
                }

                // Thay thế Fragment nếu xác định được
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.admin_menu_container, selectedFragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });

        // Cài đặt sự kiện click cho TextView Tài khoản
        btnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountFragment accountFragment = new AccountFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_menu_container, accountFragment)
                        .commit();
            }
        });
    }
}
