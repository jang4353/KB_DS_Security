package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TalkListView extends Activity  {

    ListView listView;
    TalkListAdapter talkListAdapter;
    ArrayList<TalkList_item> talkList_itemArrayList = new ArrayList<TalkList_item>();
    Button addbutton;
    SearchView searchView;
    long now;
    String uname;
    String result;
    BufferedReader reader;
    String serverip = "3.17.188.191";
    String serverport = "8888";
    String protocol = "http://";
    SharedPreferences serverDetails;
    int signal = 1;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talklist);
        listView = (ListView) findViewById(R.id.activity_talklistview);
        now = System.currentTimeMillis();

        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        Intent intent = getIntent();
        uname = intent.getStringExtra("uname");

        searchView = findViewById(R.id.searchForm);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                signal = 2;
                Toast.makeText(TalkListView.this, query, Toast.LENGTH_SHORT).show();
                searchKey = query;
                new RequestPrintTalklistTask().execute(uname);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        addbutton = (Button) findViewById(R.id.addbutton
        );
        addbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent aT = new Intent(getApplicationContext(), AddTalklist.class);
                aT.putExtra("uname", uname);
                startActivity(aT);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TalkList_item data = (TalkList_item)parent.getItemAtPosition(position);

                Bundle extras = new Bundle();
                extras.putString("title", data.getTitle());
                extras.putString("content", data.getContent());
                extras.putString("date", data.getDate());
                extras.putString("username", data.getUsername());
                extras.putString("uname", uname);
                Intent tD = new Intent(getApplicationContext(), TalklistDetail.class);
                tD.putExtra("extras", extras);
                startActivity(tD);
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        new RequestPrintTalklistTask().execute(uname);
    }

    class RequestPrintTalklistTask extends AsyncTask< String, String, String > {

        @Override
        protected String doInBackground(String...params) {

            try {
                postData(params[0]);
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IOException | JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Double result) {

        }
        protected void onProgressUpdate(Integer...progress) {

        }

        /*
        The function that makes an HTTP Post to the server endpoint that handles the
        change password operation.
        */
        public void postData(String valueIWantToSend) throws ClientProtocolException, IOException, JSONException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/printtalklist");

            List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > (3);
            nameValuePairs.add(new BasicNameValuePair("username", uname));
            if(signal == 1) {
                nameValuePairs.add(new BasicNameValuePair("signal", "Print"));
                nameValuePairs.add(new BasicNameValuePair("search", ""));
            }
            else if (signal == 2){
                nameValuePairs.add(new BasicNameValuePair("signal", "Search"));
                nameValuePairs.add(new BasicNameValuePair("search", searchKey));
            }
            else {
                nameValuePairs.add(new BasicNameValuePair("signal", "Error"));
                nameValuePairs.add(new BasicNameValuePair("search", ""));
            }

            HttpResponse responseBody;
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            responseBody = httpclient.execute(httppost);
            InputStream in = responseBody.getEntity().getContent();
            result = convertStreamToString( in );
            result = result.replace("\n", "");
           // result = result.replace("\\", "");

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (result != null) {
                        if (result.indexOf("Print Successful") != -1) {
                            //	Below code handles the Json response parsing

                            try {
                                talkList_itemArrayList.clear();
                                JSONArray jsonArray= new JSONArray(result);
                                for(int i=jsonArray.length()-2; i>=0; i--) {
                                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                                    TalkList_item talkList_item = new TalkList_item(jsonObject.getString("title"),
                                            jsonObject.getString("content"), jsonObject.getString("date"),
                                             jsonObject.getString("username"));
                                    talkList_itemArrayList.add(talkList_item);
                                }

                                talkListAdapter = new TalkListAdapter(TalkListView.this, talkList_itemArrayList);
                                listView.setAdapter(talkListAdapter);
                                signal = 1;

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),  "Print Fail", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }


        private String convertStreamToString(InputStream in ) throws IOException {
            // TODO Auto-generated method stub
            try {
                reader = new BufferedReader(new InputStreamReader( in , "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            } in .close();
            return sb.toString();
        }

    }// Added for handling menu operations
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Added for handling menu operations
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar wil
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            callPreferences();
            return true;
        } else if (id == R.id.action_exit) {
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void callPreferences() {
        // TODO Auto-generated method stub
        Intent i = new Intent(this, FilePrefActivity.class);
        startActivity(i);
    }


}
