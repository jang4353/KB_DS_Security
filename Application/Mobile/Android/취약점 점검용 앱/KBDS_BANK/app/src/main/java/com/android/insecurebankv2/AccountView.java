package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AccountView extends Activity {

    ListView listView;
    AccountAdapter accountAdapter;
    ArrayList<Account_item> account_itemArrayList = new ArrayList<Account_item>();
    Button addbutton;
    BufferedReader reader;
    String serverip = "3.17.188.191";
    String serverport = "8888";
    String protocol = "http://";
    SharedPreferences serverDetails;
    String uname;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        listView = (ListView) findViewById(R.id.activity_account);

        new AccountView.RequestAccountTask().execute(uname);

        Intent intent = getIntent();
        uname = intent.getStringExtra("uname");

        addbutton = (Button) findViewById(R.id.account_addbutton);
        addbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent aA = new Intent(getApplicationContext(), AddAccount.class);
                aA.putExtra("uname", uname);
                startActivity(aA);

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Account_item data = (Account_item)parent.getItemAtPosition(position);

                Bundle extras = new Bundle();
                extras.putString("uname", uname);
                extras.putString("account_number", data.getAccount_number());
                extras.putString("balance", data.getBalance());
                Intent aD = new Intent(getApplicationContext(), AccountDetail.class);
                aD.putExtra("extras", extras);
                startActivity(aD);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        new AccountView.RequestAccountTask().execute(uname);
    }

    @Override
    protected void onResume(){
        super.onResume();
        new AccountView.RequestAccountTask().execute(uname);
    }

    class RequestAccountTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/printaccount");

            List<NameValuePair> nameValuePairs = new ArrayList < NameValuePair > (2);
            nameValuePairs.add(new BasicNameValuePair("username", uname));
            String signal ="1";
            nameValuePairs.add(new BasicNameValuePair("signal", signal));

            HttpResponse responseBody;
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            responseBody = httpclient.execute(httppost);
            InputStream in = responseBody.getEntity().getContent();
            result = convertStreamToString( in );
            result = result.replace("\n", "");
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (result != null) {
                        if (result.indexOf("Print Successful") != -1) {
                            //	Below code handles the Json response parsing

                            try {
                                account_itemArrayList.clear();
                                JSONArray jsonArray= new JSONArray(result);

                                for(int i=0; i<jsonArray.length()-1; i++) {
                                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                                    Account_item account_item = new Account_item(jsonObject.getString("account_number"),
                                            String.valueOf(jsonObject.getInt("balance")), jsonObject.getString("username"));
                                    account_itemArrayList.add(account_item);
                                }

                                accountAdapter = new AccountAdapter(AccountView.this, account_itemArrayList);
                                listView.setAdapter(accountAdapter);

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

    }

    // Added for handling menu operations
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
