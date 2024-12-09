package com.example.book_store;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.book_store.database.CartDao;
import com.example.book_store.model.Book;
import com.example.book_store.model.CartItem;
import com.example.book_store.sharedpreferences.Constants;
import com.example.book_store.sharedpreferences.PreferenceManager;
import com.example.book_store.ui.FormatCurrency;

import java.text.DecimalFormat;

public class ShowDetailFragment extends Fragment {
    TextView txtTitle,txtPrice,txtDes,txtAuthor,txtYear,txtCate,txtNum;
    ImageView img;
    Button btnGiam,btnTang,btnAddToCart,btnBack;
    Book book;
    int numOfBook;
    CartDao cartDao;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_detail, container, false);
        txtTitle = (TextView) view.findViewById(R.id.titleTxt);
        txtPrice = (TextView) view.findViewById(R.id.priceTxt);
        img = (ImageView) view.findViewById(R.id.detail_image);
        txtDes = (TextView) view.findViewById(R.id.descriptionTxt);
        txtAuthor = (TextView) view.findViewById(R.id.detail_author);
        txtYear = (TextView) view.findViewById(R.id.detail_year);
        txtCate = (TextView) view.findViewById(R.id.detail_category);
        btnGiam = (Button) view.findViewById(R.id.btngiamsoluong);
        btnTang = (Button) view.findViewById(R.id.btntangsoloung);
        btnAddToCart = (Button) view.findViewById(R.id.detail_btnAddToCart);
        btnBack = (Button) view.findViewById(R.id.detail_btnBack);
        txtNum = (TextView)view.findViewById(R.id.txtsoluong);
        numOfBook = 1;
        Bundle bundle = getArguments();
        if(bundle != null){
            book = bundle.getParcelable("book-target");
            fillData();
        }
        cartDao = new CartDao(getContext());
        //handle event;
        //Tang giam so luong
        handleEventNumOfBook();
        handleAddToCart();
        handleBackButton();
        //Them vao gio hang
        return view;
    }
    private void handleAddToCart(){
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUserLoggedIn()) {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                else {
                    String bookId = book.getId();
                    int availableStock = book.getInStock(); // Giả sử book có thuộc tính này
                    if (bookId != null) {
                        if (numOfBook <= availableStock) {
                            // Thêm vào giỏ hàng
                            CartItem cartItem = new CartItem();
                            cartItem.setBookId(bookId);
                            cartItem.setNum(numOfBook);
                            cartDao.addToCart(cartItem);

                            // Thông báo thành công
                            Toast.makeText(getContext(), "Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // Thông báo thất bại
                            Toast.makeText(getContext(), "Số lượng sách không đủ!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
    private void changeToCartFragment(){
        CartFragment cartFragment = new CartFragment();
        this.getParentFragmentManager().beginTransaction().replace(R.id.container,cartFragment)
                .addToBackStack(null)
                .commit();
    }
//    private void handleEventNumOfBook(){
//        btnTang.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                numOfBook += 1;
//                setNum();
//            }
//        });
//        btnGiam.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (numOfBook > 1) {
//                    numOfBook -= 1;
//                    setNum();
//                }
//
//            }
//        });
//    }
    private void handleEventNumOfBook() {
        btnTang.setOnClickListener(view -> {
            if (canIncreaseQuantity(numOfBook, book.getInStock())) {
                numOfBook++;
                setNum();
            } else {
                Toast.makeText(getContext(), "Không thể tăng, số lượng sách đã đạt giới hạn!", Toast.LENGTH_SHORT).show();
            }
        });

        btnGiam.setOnClickListener(view -> {
            if (numOfBook > 1) {
                numOfBook--;
                setNum();
            }
        });
    }
    private void fillData(){
        txtTitle.setText(book.getTitle());
        txtPrice.setText(FormatCurrency.formatVND(book.getPrice()));
        Glide.with(getContext()).load(book.getImgURL()).into(img);
        txtDes.setText(book.getDescription());
        txtAuthor.setText(book.getAuthor());
        txtYear.setText(Integer.toString(book.getYear()));
        txtCate.setText(book.getCategory());
        setNum();
    }
    private void setNum(){
        txtNum.setText(Integer.toString(numOfBook));
    }
    private boolean isUserLoggedIn() {
        PreferenceManager preferenceManager = new PreferenceManager(getContext(), Constants.LOGIN_KEY_PREFERENCE_NAME);
        String phone = preferenceManager.getString(Constants.LOGIN_PHONE);
        String email = preferenceManager.getString(Constants.LOGIN_EMAIL);
        return (phone != null) || (email != null);
    }
    private void handleBackButton() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình trước đó trong ngăn xếp
                getParentFragmentManager().popBackStack();
            }
        });
    }
    private boolean canIncreaseQuantity(int currentQuantity, int stock) {
        return currentQuantity < stock;
    }
}