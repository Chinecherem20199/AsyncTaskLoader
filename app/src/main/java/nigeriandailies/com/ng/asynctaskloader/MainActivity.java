package nigeriandailies.com.ng.asynctaskloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{
    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = (EditText)findViewById(R.id.bookInput);
        mTitleText = (TextView)findViewById(R.id.titleText);
        mAuthorText = (TextView)findViewById(R.id.authorText);

        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }

    }

    public void searchBooks(View view) {
        String queryString = mBookInput.getText().toString();

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null ) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);

            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
//            new FetchBook(mTitleText, mAuthorText).execute(queryString);
//            mAuthorText.setText("");
//            mTitleText.setText(R.string.loading);
        } else {
            if (queryString.length() == 0) {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_search_term);
            } else {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_network);
            }
        }

    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = "";

        if (args != null) {
            queryString = args.getString("queryString");
        }

        return new BookLoader(this, queryString);

    }
      @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
          try {
              // Convert the response into a JSON object.
              JSONObject jsonObject = new JSONObject(data);
              // Get the JSONArray of book items.
              JSONArray itemsArray = jsonObject.getJSONArray("items");

              // Initialize iterator and results fields.
              int i = 0;
              String title = null;
              String authors = null;

              // Look for results in the items array, exiting when both the
              // title and author are found or when all items have been checked.
              while (i < itemsArray.length() &&
                      (authors == null && title == null)) {
                  // Get the current item information.
                  JSONObject book = itemsArray.getJSONObject(i);
                  JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                  // Try to get the author and title from the current item,
                  // catch if either field is empty and move on.
                  try {
                      title = volumeInfo.getString("title");
                      authors = volumeInfo.getString("authors");
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }

                  // Move to the next item.
                  i++;
              }

              // If both are found, display the result.
              if (title != null && authors != null) {
                  mTitleText.setText(title);
                  mAuthorText.setText(authors);
                  //mBookInput.setText("");
              } else {
                  // If none are found, update the UI to show failed results.
                  mTitleText.setText(R.string.no_results);
                  mAuthorText.setText("");
              }

          } catch (Exception e) {
              // If onPostExecute does not receive a proper JSON string,
              // update the UI to show failed results.
              mTitleText.setText(R.string.no_results);
              mAuthorText.setText("");
              e.printStackTrace();
          }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
