package com.android.insecurebankv2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.marcohc.toasteroid.Toasteroid;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AccountConfirm extends Activity {


    String result;
    BufferedReader reader;
    HttpResponse responseBody;
    JSONObject jsonObject;
    InputStream in ;
    String serverip = "";
    String serverport = "";
    String protocol = "http://";
    SharedPreferences serverDetails;
    public static final String MYPREFS2 = "mySharedPreferences";

    String uname = "";
    String selected_from = "";
    String selected_to = "";
    String amount ="";

    Intent intent;

    EditText confirm_password;
    Button confirm_button;

    Bundle extras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_password_confirm);

        // Get Server details from Shared Preference file.
        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        confirm_password = (EditText)findViewById(R.id.confirm_password);
        confirm_button = (Button)findViewById(R.id.confirm_button);

        intent = getIntent();
        extras = getIntent().getBundleExtra("extras");

        uname = extras.getString("uname");
        selected_from = extras.getString("selected_from");
        selected_to = extras.getString("selected_to");
        amount = extras.getString("amount");

        confirm_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new RequestDoTransferTask().execute(uname);

            }
        });
    }

    public class RequestDoTransferTask extends AsyncTask< String, String, String > {

        /**
         * constructor
         * @return
         */
        public void AsyncHttpTransferPost(String string) {
            //do something
        }

        /**
         * background functions
         */
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String...params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/dotransfer");
            SharedPreferences settings = getSharedPreferences(MYPREFS2, 0);

            List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >(5);
            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("password", confirm_password.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("from_acc", selected_from));
            nameValuePairs.add(new BasicNameValuePair("to_acc", selected_to));
            nameValuePairs.add(new BasicNameValuePair("amount", amount));
            try {
                //	The HTTP Post of the credentials plus the transaction information
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                //	Stores the HTTP response of the transaction activity
                responseBody = httpclient.execute(httppost);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try { in = responseBody.getEntity().getContent();
            } catch (IllegalStateException | IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                result = convertStreamToString( in );
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            result = result.replace("\n", "");
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    AsyncHttpTransferPost("result");
                    if (result != null) {
                        if (result.indexOf("Success") != -1) {
                            Toasteroid.show(AccountConfirm.this, "Transfer Successful!!", Toasteroid.STYLES.SUCCESS, Toasteroid.LENGTH_SHORT);

                            try {
                                jsonObject = new JSONObject(result);
                                finish();

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else {
                            Toasteroid.show(AccountConfirm.this, "Transfer Failed!!", Toasteroid.STYLES.ERROR, Toasteroid.LENGTH_SHORT);

                        }
                    }
                }

            });
            return null;
        }

        @Override
        protected void onPostExecute(String result) {}

        protected void onProgressUpdate(String...progress) {}
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
