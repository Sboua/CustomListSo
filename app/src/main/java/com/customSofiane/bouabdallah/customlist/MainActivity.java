package com.customSofiane.bouabdallah.customlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.squareup.picasso.Picasso;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    ArrayList<HashMap<String, String>> collectionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        collectionsList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new GetCollections().execute();
    }

    private class GetCollections extends AsyncTask<Void, Void, Void> {

    JSONObject jsonObj;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(MainActivity.this,"Json Data is loading",Toast.LENGTH_LONG).show();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String url = "https://shopicruit.myshopify.com/admin/custom_collections.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";
        String jsonStr = sh.makeServiceCall(url);
        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                jsonObj = new JSONObject(jsonStr);
                JSONArray collections = jsonObj.getJSONArray("custom_collections");
                // looping through All collections
                for (int i = 0; i < collections.length(); i++) {
                    JSONObject c = collections.getJSONObject(i);
                    String id = c.getString("id");
                    String handle = c.getString("handle");
                    String title = c.getString("title");
                    JSONObject img = c.getJSONObject("image");
                    String image_src = img.getString("src");

                    // tmp hash map for single collection
                    HashMap<String, String> collection = new HashMap<>();
                    // adding each child node to HashMap key => value
                    collection.put("id", id);
                    collection.put("handle", handle);
                    collection.put("title", title);
                    collection.put("logo", image_src);
                    collectionsList.add(collection);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        } else {
            Log.e(TAG, "Couldn't get json from server.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        ///had to fix twikk the adapter to add the imageview;
        ListAdapter adapter = new SimpleAdapter(MainActivity.this, collectionsList, R.layout.list_item, new String[]{ "title","handle"},
                new int[]{R.id.title, R.id.handle}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row =  super.getView(position, convertView, parent);
                    ImageView right = (ImageView)row.findViewById(R.id.imgCol);
                    JSONArray collections = null;
                    try {
                        collections = jsonObj.getJSONArray("custom_collections");
                        JSONObject jClicked = collections.getJSONObject(position);
                        Picasso.get().load(jClicked.getJSONObject("image").getString("src")).into(right);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return row;
                }
        };
        lv.setAdapter(adapter);
        //add the click to get to detailPage
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView parent, View v, int position, long id){
                try {
                    JSONArray collections = jsonObj.getJSONArray("custom_collections");
                    JSONObject jClicked = collections.getJSONObject(position);
                    Intent intent = new Intent(getApplicationContext(), DetailPage.class);
                    intent.putExtra("json", jClicked.toString());
                    startActivity(intent);
                }catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
}
