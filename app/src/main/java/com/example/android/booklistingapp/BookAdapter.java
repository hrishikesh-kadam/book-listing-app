package com.example.android.booklistingapp;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> implements Serializable{

    public BookAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Book> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Book currentBook = getItem(position);
        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        textViewTitle.setText(currentBook.getTitle());
        TextView textViewAuthor = convertView.findViewById(R.id.textViewAuthor);
        textViewAuthor.setText(currentBook.getAuthor());

        return convertView;
    }
}
