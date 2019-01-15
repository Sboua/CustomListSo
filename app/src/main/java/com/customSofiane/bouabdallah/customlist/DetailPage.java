package com.customSofiane.bouabdallah.customlist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DetailPage extends AppCompatActivity {

    private String TAG = DetailPage.class.getSimpleName();
    private JSONObject collection;
    ArrayList<HashMap<String, String>> productList;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_page);
        productList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        //always check for null object to avoid crash.
        //get the json object clicked.
        if (getIntent().getStringExtra("json") != null) {
            String dataExtra = getIntent().getStringExtra("json");
            try {
                collection = new JSONObject(dataExtra);
                ImageView imgCollection = (ImageView) findViewById(R.id.imageView);
                JSONObject img_uri = collection.getJSONObject("image");
                TextView title = (TextView) findViewById(R.id.title) ;
                title.setText(collection.getString("title"));
                title = (TextView) findViewById(R.id.html_body);
                title.setText(collection.getString("body_html"));
                Picasso.get().load(img_uri.getString("src")).into(imgCollection);
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
        }
        new GetProducts().execute();
    }

        private class GetProducts extends AsyncTask<Void, Void, Void> {

        JSONObject jsonObj,productObj;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(DetailPage.this,"Loading all the products",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            String url = null;
            String ids = "";
            try {
                url = "https://shopicruit.myshopify.com/admin/collects.json?collection_id="+collection.getString("id")+"&page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Making a request to url and getting response

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray collections = jsonObj.getJSONArray("collects");

                    // looping through All Contacts
                    int k = 0;
                    for (int i = 0; i < collections.length(); i++) {
                        JSONObject c = collections.getJSONObject(i);
                        if (ids.equals("")) {
                            ids = c.getString("product_id") + ","; //getting first id
                        } else
                            ids = ids + c.getString("product_id") + ","; //concat all the ids to add to the url
                    }
                        String productsStr = sh.makeServiceCall("https://shopicruit.myshopify.com/admin/products.json?ids="+ids+"&page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6");
                        if (productsStr != null) {
                            productObj = new JSONObject(productsStr);
                            JSONArray products = productObj.getJSONArray("products");
                            for(int j = 0; j <products.length();j++){//looping through the products
                                String title = products.getJSONObject(j).getString("title"); //getting the title
                                String name = products.getJSONObject(j).getJSONArray("options").getJSONObject(0).getString("name"); //getting the product name
                                JSONArray variantsArray = products.getJSONObject(j).getJSONArray("variants"); //getting the variants jsonArray
                                int numAvail =0; //counting the qty in inventory
                                for(int l = 0 ; l<variantsArray.length();l++){
                                    numAvail+= variantsArray.getJSONObject(l).getInt("inventory_quantity");
                                }
                               String image_src = products.getJSONObject(j).getJSONObject("image").getString("src");
                                HashMap<String, String> collectionMap = new HashMap<>();
                                collectionMap.put("productName",name);

                                collectionMap.put("title",title);
                                collectionMap.put("inventoryAv",Integer.toString(numAvail));
                                collectionMap.put("img",image_src);
                                productList.add(collectionMap);
                            }

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

            ListAdapter adapter = new SimpleAdapter(DetailPage.this, productList, R.layout.product_item, new String[]{ "productName","title","inventoryAv"},
                    new int[]{R.id.productName, R.id.title,R.id.inventoryAv}){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row =  super.getView(position, convertView, parent);
                    ImageView right = (ImageView)row.findViewById(R.id.imgCol);
                    Picasso.get().load(productList.get(position).get("img")).into(right);;

                    return row;
                }
            };

            lv.setAdapter(adapter);
        }
    }


}
