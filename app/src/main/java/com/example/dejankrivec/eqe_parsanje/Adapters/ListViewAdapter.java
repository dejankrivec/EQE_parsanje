package com.example.dejankrivec.eqe_parsanje.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dejankrivec.eqe_parsanje.R;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dejan Krivec on 29. 11. 2016.
 */

public class ListViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;
    //ImageLoader imageLoader;
    HashMap<String, String> item = new HashMap<String, String>();

    public ListViewAdapter(Context context,
                           ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        data = arraylist; // get data

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolderItem holder; // Avoid from calling findviewbyid each time
        View ItemView = convertView;

        if(ItemView==null){

            // invoke layout
            ItemView = inflater.inflate(R.layout.listview_reddit, null);

            // find all views in layout
            holder = new ViewHolderItem();
            holder.Title = (TextView) ItemView.findViewById(R.id.Title);
            holder.Tagline=(TextView)ItemView.findViewById(R.id.Tagline);
            holder.Flat=(TextView) ItemView.findViewById(R.id.Flat);
            holder.Image =(ImageView) ItemView.findViewById(R.id.image);

            // save holder with view
            ItemView.setTag( holder );
        }
        else
            holder=(ViewHolderItem)ItemView.getTag();

        if(data.size()>0)
        {
            // get data from array
            item = data.get(position);

            // set values of item to view
            holder.Title.setText(item.get("Title").toString());
            holder.Tagline.setText(item.get("TagLine").toString());
            holder.Flat.setText(item.get("Flat").toString());

            if (holder.Image != null) {
                // call asynctask loading image for smooth scrooling
                new LoadImage(holder.Image).execute(item.get("ImgPath").toString());
            }
        }

        ItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"You clicked on item " + String.valueOf(position),Toast.LENGTH_SHORT).show();
            }
        });
        return ItemView;
    }
    private class ViewHolderItem{
        TextView Title;
        TextView Tagline;
        TextView Flat;
        ImageView Image;
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap >{

        private final WeakReference<ImageView> imageViewReference; // prevent overflowed memory

        public LoadImage(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView); // set weak reference to imageView
            int z = 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            return CreatBitmapFromUrl(params[0]); // creat image from path
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap); // set image
                }
            }
        }
    }
    private Bitmap CreatBitmapFromUrl(String url) {
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream((InputStream)new URL("http:" + url).getContent());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bm == null){ // if image doesnt exist
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.noimage);
        }
        return bm;
    }
}
