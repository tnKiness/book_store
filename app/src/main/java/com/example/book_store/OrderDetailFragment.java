package com.example.book_store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.book_store.customadapter.OrderedItemAdapter;
import com.example.book_store.model.OrderedItem;
import com.example.book_store.sharedpreferences.Constants;
import com.example.book_store.sharedpreferences.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderDetailFragment extends Fragment {
    Button btnBack;
    private String orderId, orderBy;
    private TextView txtOrderId, txtDate, txtOrderStatus, txtTotalItems, txtAmount,txtOderBy;
    private RecyclerView itemsRv;
    private String phone;
    private Context context;
    private ArrayList<OrderedItem> orderedItemList;
    private OrderedItemAdapter orderedItemAdapter;
    int isAdmin;


    public OrderDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PreferenceManager preferenceManager = new PreferenceManager(getContext(),Constants.LOGIN_KEY_PREFERENCE_NAME);
        isAdmin = preferenceManager.getInt(Constants.LOGIN_IS_ADMIN);
        int idLayout = R.layout.fragment_order_detail;
        if(isAdmin == 1){
            idLayout = R.layout.fragment_order_detail_admin;
        }
        View view = inflater.inflate(idLayout, container, false);
        txtOrderId = view.findViewById(R.id.txtOrderId);
        txtDate = view.findViewById(R.id.txtDate);
        txtAmount = view.findViewById(R.id.txtAmount);
        txtTotalItems = view.findViewById(R.id.txtTotalItems);
        txtOrderStatus = view.findViewById(R.id.txtOrderStatus);
        itemsRv = (RecyclerView) view.findViewById(R.id.itemsRv);
        if(isAdmin == 1){
            txtOderBy = view.findViewById(R.id.txtOrderBy);
        }
        Bundle bundle = getArguments();
        if(bundle != null){
            orderId = bundle.getString("orderId");
            orderBy = bundle.getString("orderBy");
        }
        phone = preferenceManager.getString(Constants.LOGIN_PHONE);
        loadOrderDetail();
        btnBack = view.findViewById(R.id.order_detail_btnBack);
        handleBackButton();
        return view;
    }

    private void loadOrderedItems() {
        orderedItemList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderedItemList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    OrderedItem orderedItem = ds.getValue(OrderedItem.class);
                    orderedItemList.add(orderedItem);
                }
                orderedItemAdapter = new OrderedItemAdapter(getContext(),orderedItemList);
                itemsRv.setAdapter(orderedItemAdapter);
                txtTotalItems.setText(""+snapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadOrderDetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String orderBy = ""+snapshot.child("orderBy").getValue();
                String orderCost = ""+snapshot.child("orderCost").getValue();
                String orderId = ""+snapshot.child("orderId").getValue();
                String orderStatus = ""+snapshot.child("orderStatus").getValue();
                String orderTime = ""+snapshot.child("orderTime").getValue();

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(orderTime));
                String formatedTime = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();

                if (orderStatus.equals("In progress")){
                    txtOrderStatus.setTextColor(ContextCompat.getColor(context,R.color.primary_color));
                } else if (orderStatus.equals("Completed")){
                    txtOrderStatus.setTextColor(ContextCompat.getColor(context,R.color.second_color));
                } else if (orderStatus.equals("Cancelled")){
                    txtOrderStatus.setTextColor(ContextCompat.getColor(context,R.color.text_second));
                }

                txtOrderId.setText(orderId);
                txtDate.setText(formatedTime);
                txtOrderStatus.setText(orderStatus);
                txtAmount.setText(orderCost);
                if(isAdmin == 1)
                    txtOderBy.setText(orderBy);
                loadOrderedItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void handleBackButton() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                int backStackCount = fragmentManager.getBackStackEntryCount();

                if (backStackCount > 0) {
                    // Lấy BackStackEntry trước đó
                    FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(backStackCount - 1);

                    // Kiểm tra nếu là CheckOutFragment thì thay thế bằng HomeFragment
                    if ("CheckOutFragment".equals(entry.getName())) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, new HomeFragment())
                                .commit();
                    } else {
                        // Nếu không, quay lại Fragment trước đó
                        fragmentManager.popBackStack();
                    }
                } else {
                    // Nếu không còn gì trong BackStack, thay thế bằng HomeFragment
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, new HomeFragment())
                            .commit();
                }
            }
        });
    }
}