package com.example.dejankrivec.eqe_parsanje;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dejankrivec.eqe_parsanje.Adapters.ListViewAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    // Use ButterKnife , avoid from calling findViewById
    Context context;
    /*Bind(R.id.parse_button)
    Button parse;

    @Bind(R.id.tw)
    TextView tw;*/

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @Bind(R.id.list_items)
    ListView listview;

    String url = "https://www.reddit.com/rising/";
    LocationManager mLocationManager;

    ProgressDialog mProgressDialog;
    ListViewAdapter adapter; // instance to listView adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        ButterKnife.bind(this);

        if(isNetworkAvailable()) // Chek if device have enabled internet
        {
            new ParseData().execute(url); // call asynctask to parse data from input url
            swipeContainer.setRefreshing(true); // show refreshing icon
        }
        else
            Toast.makeText(context, "No internet avaliable", Toast.LENGTH_LONG).show();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                if(isNetworkAvailable()) // Chek if device have enabled internet
                {
                    new ParseData().execute(url); // lahko globalno defirniramo url lahko pa poslejmo kot parameter
                }else
                    Toast.makeText(context, "No internet avaliable", Toast.LENGTH_LONG).show();
            }

        });


        /*parse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get network status
                if(isNetworkAvailable()) // Chek if device have enabled internet
                    new ParseData().execute(url); // lahko globalno defirniramo url lahko pa poslejmo kot parameter
                else
                    Toast.makeText(context, "No internet avaliable", Toast.LENGTH_LONG).show();
            }
        });*/
    }
    private boolean isNetworkAvailable() { // check for internet status
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // return true if internet is available and false if not
        // check both because of error if activeNetwork is null then we cannot call isConnected which will cause error
    }

    // 1.param = doInBackground input param,
    // 2.param for onProgressUpdate we can update dialog,
    // 3.param onPostExectute input param type and return type for doInBackground
    private class ParseData extends AsyncTask<String, Void, Void> {
        String desc;
        ArrayList list = new ArrayList<HashMap<String, String>>(); // to store hasmaps which holds value of each item

        public ParseData Progress;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Progress = this;
            /*mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Parsing data");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();*/
            new CountDownTimer(8000, 8000) { // check connection timeout, if internet access is limited, after 4 sec, stop parsing data.
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    // stop async task if not in progress
                    if (Progress.getStatus() == AsyncTask.Status.RUNNING) { // check if asyncTask still running
                        Progress.cancel(false);
                        swipeContainer.setRefreshing(false); // close updating dialog
                        Toast.makeText(context, "No internet avaliable", Toast.LENGTH_LONG).show();
                    }
                }
            }.start();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                // Connect to the web site
                Document document = Jsoup.connect(params[0]).get(); // parse data from url

                // Using Elements to get the Meta data
                Element content = document.select("div#siteTable").first(); // get content from specific div on website


                for(Element el : content.children().not("div.clearleft").not("div.nav-buttons")){ // go thru all childs of parent div
                    HashMap<String, String> map = new HashMap<String, String>(); // create hasmap to store values
                    int z = 0;
                    //String p = img.attr("src");
                    String ImgPath = el.select("img").attr("src"); // Image path
                    String Title = el.select("p.title").select("a").text(); // Title
                    String TagLine = el.select("p.tagline").text(); // Tagline - submited when/who
                    String Flat = el.select("ul.flat-list").text(); // Comments & share button


                    map.put("ImgPath",ImgPath); // Image Path
                    map.put("Title",Title); // Title
                    map.put("TagLine",TagLine); // Tagline - submited when/who
                    map.put("Flat",Flat); // Comments & share button

                    list.add(map); // add hashmap to array

                }

                desc = "data from: " + document.title();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set description into TextView
            //tw.setText(desc); // add title of website to view
            adapter = new ListViewAdapter(MainActivity.this,list); // call listview adapter;
            listview.setAdapter(adapter); // fill listview;
            //mProgressDialog.dismiss(); // close dialog
            swipeContainer.setRefreshing(false); // close updating dialog
        }
    }


}
