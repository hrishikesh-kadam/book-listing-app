package com.example.android.booklistingapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import android.widget.SearchView;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>>{

    private static final String LOG_TAG = BookLoader.class.getName();
    private SearchView searchView;

    public BookLoader(Context context, SearchView searchView) {
        super(context);
        Log.v(LOG_TAG, "-> BookLoader");
        this.searchView = searchView;
    }

    @Override
    protected void onStartLoading() {
        Log.v(LOG_TAG, "-> onStartLoading");
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        Log.v(LOG_TAG, "-> loadInBackground");
        String searchQuery = searchView.getQuery().toString();

        if( searchQuery.isEmpty() )
            return null;
        else {
            String booksBaseUrl = getContext().getResources().getString(R.string.baseUrl);
            String key = getContext().getResources().getString(R.string.key);
            List<Book> books = QueryUtils.fetchBookQuery(booksBaseUrl, searchQuery, key);
            return books;
        }
    }
}
