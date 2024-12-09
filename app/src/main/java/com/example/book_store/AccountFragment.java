package com.example.book_store;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.book_store.model.User;
import com.example.book_store.sharedpreferences.Constants;
import com.example.book_store.sharedpreferences.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AccountFragment extends Fragment {
    TextView txtName;
    TextView txtPhone;
    LinearLayout groupBtn;
    Button btnLogout;
    Button btnInfor;
    Button btnChangePassword;
    Button btnOrder;
    Fragment orderFragment = new OrderFragment();
    Fragment inforFragment = new InforFragment();
    Fragment changePassFragment = new ChangePasswordFragment();
    private DatabaseReference reference;
    private String phone, email;
    PreferenceManager preferenceManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        txtName = view.findViewById(R.id.account_txtUserName);
        txtPhone = view.findViewById(R.id.account_txtUserPhone);
        btnOrder = view.findViewById(R.id.account_btnBills);
        btnLogout = view.findViewById(R.id.account_btnLogOut);
        btnInfor = view.findViewById(R.id.account_btnInformation);
        btnChangePassword = view.findViewById(R.id.account_btnChangePass);
        groupBtn = (LinearLayout) view.findViewById(R.id.account_group_btn);

        preferenceManager = new PreferenceManager(getContext(), Constants.LOGIN_KEY_PREFERENCE_NAME);
        phone = preferenceManager.getString(Constants.LOGIN_PHONE);
        email = preferenceManager.getString(Constants.LOGIN_EMAIL);

        if(phone == null && email == null){
            fillData("Đăng nhập/Đăng ký","");
            txtName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
        else {
            if (phone != null && !phone.isEmpty()){
                reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User profile = snapshot.getValue(User.class);

                        if (profile != null){
                            String name = profile.getFullName();
                            fillData(name,phone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else {
                // Đăng nhập bằng Google Sign-In
                fillData(email, "Đăng nhập bằng Google");
            }

            groupBtn.setVisibility(View.VISIBLE);
            handleOrder();
            handleInfor();
            handleLogout();
            handleChangePass();
        }

        return view;
    }
    private void fillData(String name,String phone){
        txtName.setText(name);
        txtPhone.setText(phone);
    }

    private void handleOrder() {
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem người dùng có phải là admin hay không
                if (preferenceManager.getInt(Constants.LOGIN_IS_ADMIN) == 1) {
                    // Nếu là admin, thay thế fragment trong container dành cho admin
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.admin_menu_container, orderFragment)  // Thay thế fragment trong container admin
                            .addToBackStack(null)  // Thêm vào ngăn xếp nếu muốn quay lại fragment trước đó
                            .commit();
                } else {
                    // Nếu là người dùng bình thường, thay thế fragment trong container chính
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.container, orderFragment)  // Thay thế fragment trong container chính
                            .addToBackStack(null)  // Thêm vào ngăn xếp nếu muốn quay lại fragment trước đó
                            .commit();
                }
            }
        });
    }

    private void handleLogout(){
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preferenceManager == null)
                    return;
                preferenceManager.clear();
                Intent intent = new Intent(getContext(),MenuActivity.class);
                startActivity(intent);
            }
        });
    }
    private void handleInfor(){
        btnInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(preferenceManager.getInt(Constants.LOGIN_IS_ADMIN) == 1) {
                    // Dành cho Admin, thay thế fragment trong container admin
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.admin_menu_container, inforFragment)
                            .addToBackStack(null)  // Nếu muốn giữ lại ngăn xếp
                            .commit();
                } else {
                    // Dành cho người dùng bình thường, thay thế fragment trong container chính
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.container, inforFragment)
                            .addToBackStack(null)  // Nếu muốn giữ lại ngăn xếp
                            .commit();
                }
            }
        });
    }
    private void handleChangePass(){
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceManager.getInt(Constants.LOGIN_IS_ADMIN) == 1) {
                    // Dành cho Admin, thay thế fragment trong container admin
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.admin_menu_container, changePassFragment)
                            .addToBackStack(null)  // Nếu muốn giữ lại ngăn xếp
                            .commit();
                } else {
                    // Dành cho người dùng bình thường, thay thế fragment trong container chính
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.container, changePassFragment)
                            .addToBackStack(null)  // Nếu muốn giữ lại ngăn xếp
                            .commit();
                }
            }
        });
    }
}