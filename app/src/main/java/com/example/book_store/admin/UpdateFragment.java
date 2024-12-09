package com.example.book_store.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.book_store.R;
import com.example.book_store.database.BookDao;
import com.example.book_store.model.Book;
import com.example.book_store.ui.FormatCurrency;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class UpdateFragment extends Fragment {

    //Realtime Database
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    //Firebase Storage
    FirebaseStorage storage;
    StorageReference storageReference;
    //Data biding
    EditText txtTitle, txtAuthor,txtYear,txtPrice,txtNum,txtDes;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swActive;
    Spinner snCategory;
    Button btnUpdate,btnAddImg;
    Book book;
    ArrayList<String>categorys;
    ArrayAdapter categoryAdapter;
    //
    ActivityResultLauncher<String> getImage;
    //
    AlertDialog dialog;
    //
    BookDao dao;
    EditText txtImageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_update, container, false);
        //get Realtime
        database = FirebaseDatabase.getInstance();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        //biding
        book = new Book();
        txtImageUrl = (EditText) view.findViewById(R.id.crud_et_url);
        txtTitle = (EditText) view.findViewById(R.id.crud_title);
        txtAuthor = (EditText) view.findViewById(R.id.crud_author);
        txtYear = (EditText) view.findViewById(R.id.crud_year);
        txtPrice = (EditText) view.findViewById(R.id.crud_price);
        txtNum = (EditText) view.findViewById(R.id.crud_in_stock);
        txtDes = (EditText) view.findViewById(R.id.crud_des);
        swActive = (Switch) view.findViewById(R.id.crud_sw_active);
        snCategory = (Spinner) view.findViewById(R.id.crud_categorys);
        //Book DAO
        dao = new BookDao(getContext());
        //btn update
        btnUpdate = (Button) view.findViewById(R.id.crud_btn_update);
        //Fill data category
        categorys = new ArrayList<>();
        categoryAdapter = new ArrayAdapter(getContext(),R.layout.style_spinner,categorys);
        snCategory.setAdapter(categoryAdapter);
        getCategory();
        //get arguments
        Bundle bundle = getArguments();
        if(bundle != null){
            String bookId = bundle.getString("BOOK_ID");
            book.setId(bookId);
            getBookInfo();
        }
        //Dialog
        setProgressDialog();
        //handle event
        onUpdateClick();
        return view;
    }
    private void onUpdateClick(){
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check data valid
                String title = txtTitle.getText().toString();
                String author = txtAuthor.getText().toString();
                String category = String.valueOf(snCategory.getSelectedItem());
                String year = txtYear.getText().toString();
                String price = txtPrice.getText().toString();
                String inStock = txtNum.getText().toString();
                String desc = txtDes.getText().toString();
                int isActive = 1;
                if(!swActive.isChecked()){
                    isActive = 0;
                }
                if (isValid(title, author, category, year, price, inStock, desc)) {
                    // Kiểm tra URL trong EditText
                    String inputImageUrl = txtImageUrl.getText().toString().trim();
                    if (!inputImageUrl.isEmpty()) {
                        book.setImgURL(inputImageUrl);
                        updateBookToDatabase(book, title, author, category, year, price, inStock, desc);
                    } else {
                        // Nếu không có ảnh từ URL
                        Toast.makeText(getContext(), "Hãy nhập URL", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }
    private void getBookInfo(){
        myRef = database.getReference("Books");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(book.getId())){
                    book = snapshot.child(book.getId()).getValue(Book.class);
                    fillData();
                }
                else{
                    Toast.makeText(getContext(), "Không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void fillData(){
        txtTitle.setText(book.getTitle());
        txtAuthor.setText(book.getAuthor());
        ArrayAdapter adapter = (ArrayAdapter) snCategory.getAdapter();
        if(adapter != null){
            int selectIndex = adapter.getPosition(book.getCategory());
            snCategory.setSelection(selectIndex);
        }
        txtPrice.setText(Integer.toString(book.getPrice()));
        txtNum.setText(Integer.toString(book.getInStock()));
        txtYear.setText(Integer.toString(book.getYear()));
        txtDes.setText(book.getDescription());
        swActive.setChecked(1 == book.getIsActive());
    }
    private void getCategory(){
        myRef = database.getReference("Categorys");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data:snapshot.getChildren()){
                    String cate = data.getValue(String.class);
                    categorys.add(cate);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean isValid(String title,String category,String author,String year,String price,String inStock,String desc){
        if(title.trim().isEmpty() || author.trim().isEmpty() || year.trim().isEmpty() ||
                category.trim().isEmpty() || price.trim().isEmpty() || inStock.trim().isEmpty() || desc.trim().isEmpty()){
            Toast.makeText(getContext(), "Các trường không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }
        int y = Integer.parseInt(year);
        int num = Integer.parseInt(inStock);
        int p = Integer.parseInt(price);
        if(y < 0 && y > Year.now().getValue()){
            Toast.makeText(getContext(), "Năm xuất bản không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(num < 0){
            Toast.makeText(getContext(), "Số lượng phải lớn hơn hoặc bằng 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(p<=0){
            Toast.makeText(getContext(), "Giá bán phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private  void clearEditText(){
        txtTitle.setText("");
        txtAuthor.setText("");
        txtDes.setText("");
        txtNum.setText("");
        txtPrice.setText("");
        txtYear.setText("");
    }
    //progress dialog
    private void setProgressDialog() {

        int llPadding = 30;
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(getContext());
        tvText.setText("Đang tải ảnh lên...");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setView(ll);

        dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }
    private void updateBookToDatabase(Book book, String title, String author, String category, String year, String price, String inStock, String desc) {
        if (dao.updateBook(book.getId(), title, author, category, book.getImgURL(), Integer.parseInt(year), Integer.parseInt(price), Integer.parseInt(inStock), desc, book.getIsActive())) {
            clearEditText();
            Toast.makeText(getContext(), "Cập nhật sách thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Cập nhật sách thất bại", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
}