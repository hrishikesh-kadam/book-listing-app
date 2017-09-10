package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int BOOK_LOADER_ID = 123;
    private ListView listView;
    private BookAdapter bookAdapter;
    private ProgressBar progressBar;
    private SearchView searchView;
    private TextView emptyView;
    private String lastSearchQuery;
    private boolean isSearchViewFocused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "-> onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);
        bookAdapter = new BookAdapter(this, 0, new ArrayList<Book>());
        listView.setAdapter(bookAdapter);
        emptyView = (TextView) findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        final LoaderManager loaderManager = getLoaderManager();

        searchView = (SearchView) findViewById(R.id.searchview);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.v(LOG_TAG, "-> onQueryTextSubmit");

                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo == null || !networkInfo.isConnected()) {
                    emptyView.setText(R.string.noConnection);
                    bookAdapter.clear();
                    return false;
                }

                emptyView.setText("");
                progressBar.setVisibility(View.VISIBLE);

                if (lastSearchQuery == null || lastSearchQuery.equalsIgnoreCase(s))
                    loaderManager.initLoader(BOOK_LOADER_ID, null, MainActivity.this);
                else {
                    bookAdapter.clear();
                    loaderManager.restartLoader(BOOK_LOADER_ID, null, MainActivity.this);
                }

                lastSearchQuery = s;
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book currentBook = bookAdapter.getItem(i);
                Uri earthquakeUri = Uri.parse(currentBook.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);
                startActivity(websiteIntent);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "-> onPause");
        isSearchViewFocused = checkFocusRec(searchView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "-> onSaveInstanceState -> " + listView.getFirstVisiblePosition());
        outState.putSerializable("bookAdapter", bookAdapter);
        outState.putInt("scrollPosition", listView.getFirstVisiblePosition());
        outState.putString("emptyViewText", emptyView.getText().toString());
        outState.putString("lastSearchQuery", lastSearchQuery);
        outState.putBoolean("isChangingConfigurations", isChangingConfigurations());
        outState.putString("currentSearchQuery", searchView.getQuery().toString());
        outState.putBoolean("isSearchViewFocused", isSearchViewFocused);
        Log.v(LOG_TAG, "-> onSaveInstanceState -> " + isSearchViewFocused);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG, "-> onRestoreInstanceState");
        bookAdapter = (BookAdapter) savedInstanceState.getSerializable("bookAdapter");
        listView.setAdapter(bookAdapter);
        listView.setSelection(savedInstanceState.getInt("scrollPosition"));
        emptyView.setText(savedInstanceState.getString("emptyViewText"));
        lastSearchQuery = savedInstanceState.getString("lastSearchQuery");

        boolean isChangingConfigurations = savedInstanceState.getBoolean("isChangingConfigurations");
        if (isChangingConfigurations) {
            searchView.setQuery(savedInstanceState.getString("currentSearchQuery"), false);
            if( !savedInstanceState.getBoolean("isSearchViewFocused") )
                findViewById(R.id.rootview).requestFocus();
        } else {
            searchView.setQuery(lastSearchQuery, true);
            findViewById(R.id.rootview).requestFocus();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG, "-> onRestart");
        searchView.setQuery(lastSearchQuery, false);

        if (bookAdapter.getCount() == 0)
            return;

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            emptyView.setText(R.string.noConnection);
            bookAdapter.clear();
            return;
        }

        emptyView.setText("");
        progressBar.setVisibility(View.VISIBLE);
        bookAdapter.clear();
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "-> onCreateLoader");
        SearchView searchView = (SearchView) findViewById(R.id.searchview);
        return new BookLoader(this, searchView);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        Log.v(LOG_TAG, "-> onLoadFinished");
        bookAdapter.clear();

        //if (!(books != null && !books.isEmpty())) {
        if (books != null && !books.isEmpty())
            bookAdapter.addAll(books);
        else if (!searchView.getQuery().toString().isEmpty())
            emptyView.setText(R.string.noResultFound);

        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.v(LOG_TAG, "-> onLoaderReset");
    }

    private boolean checkFocusRec(View view) {
        if (view.isFocused())
            return true;

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (checkFocusRec(viewGroup.getChildAt(i)))
                    return true;
            }
        }
        return false;
    }
}
