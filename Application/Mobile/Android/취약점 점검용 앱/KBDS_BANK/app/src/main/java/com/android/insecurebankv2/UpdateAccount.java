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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class UpdateAccount extends Activity {

    Bundle extras;
    JSONObject jsonObject = new JSONObject();
    String result;
    BufferedReader reader;
    String serverip = "";
    String serverport = "";
    String protocol = "http://";
    SharedPreferences serverDetails;
    Intent intent;
    String uname;
    String account_number;
    String balance;
    Button update_button;
    EditText update_balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_update);

        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        intent = getIntent();
        extras = getIntent().getBundleExtra("extras");

        uname = extras.getString("uname");
        account_number = extras.getString("account_number");
        balance = extras.getString("balance");
        update_balance = (EditText)findViewById(R.id.update_balance);


        update_button = (Button)findViewById(R.id.update_button);
        update_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                new UpdateAccount.RequestUpdateAccountTask().execute(uname);
                finish();

            }
        });


    }

    class RequestUpdateAccountTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/updateaccount");
            List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >(3);

            StringTokenizer s = new StringTokenizer(uname);
            while(s.hasMoreTokens()) {
                Log.e("DONGUK",s.nextToken(":"));
            }

            int temp = Integer.parseInt(balance) + Integer.parseInt(update_balance.getText().toString());
            balance = String.valueOf(temp);
            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("account_number", account_number));
            nameValuePairs.add(new BasicNameValuePair("balance", balance));

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
                        if (result.indexOf("Update Successful") != -1) {
                            //	Below code handles the Json response parsing
                            JSONObject jsonObject;

                            try {
                                jsonObject = new JSONObject(result);
                                String login_response_message = jsonObject.getString("message");
                                Toast.makeText(getApplicationContext(), login_response_message, Toast.LENGTH_LONG).show();
                                finish();

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),  "Update Fail", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),  "Update Fail", Toast.LENGTH_LONG).show();
                        finish();
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
