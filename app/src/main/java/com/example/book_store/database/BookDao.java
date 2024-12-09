package com.example.book_store.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.book_store.model.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BookDao {
    FirebaseDatabase database;
    DatabaseReference myRef;
    Book book;
    Context context;
    public BookDao(Context context) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Books");
        this.context = context;
    }
    public void addBook(Book book, OnCompleteListener<Void> callback) {
        // Add to DB
        String id = myRef.push().getKey();
        book.setId(id);

        // Set value and attach callback
        myRef.child(book.getId()).setValue(book).addOnCompleteListener(callback);
    }
    public boolean updateBook(String id,String title,String author,String category,String imgURL,int year,int price,int inStock,String desc,int isActive){
        final boolean[] result = new boolean[1];
        HashMap Book = new HashMap();
        Book.put("id",id);
        Book.put("title", title);
        Book.put("author",author);
        Book.put("category",category);
        Book.put("price",price);
        Book.put("year",year);
        Book.put("inStock",inStock);
        Book.put("description",desc);
        Book.put("imgURL",imgURL);
        Book.put("isActive",isActive);
        myRef = database.getReference("Books");
        myRef.child(id).updateChildren(Book).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    result[0] = true;
                }
                else{
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    result[0] = false;
                }
            }
        });
        return result[0];
    }
    public boolean updateBookInStock(String id,int minus){
        final boolean[] result = new boolean[1];
        myRef = database.getReference("Books");
        myRef.child(id).child("inStock").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int oldInStock = snapshot.getValue(Integer.class);
                int newInStocck = oldInStock - minus;
                myRef.child(id).child("inStock").setValue(newInStocck).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        result[0]=true;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result[0] = false;
            }
        });
        return  result[0];
    }

}
