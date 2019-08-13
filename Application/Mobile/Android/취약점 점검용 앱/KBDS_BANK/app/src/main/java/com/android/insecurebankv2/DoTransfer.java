package com.android.insecurebankv2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
import org.w3c.dom.Text;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.marcohc.toasteroid.Toasteroid;

/*
The page that allows the user to transfer an amount between two accounts
@author Dinesh Shetty
*/
public class DoTransfer extends Activity {

	String result;
	BufferedReader reader;

    Spinner from;
    Spinner to;

	EditText amount;

	Button transfer;
	HttpResponse responseBody;
	JSONObject jsonObject;
	InputStream in ;
	String serverip = "";
	String serverport = "";
	String protocol = "http://";
	SharedPreferences serverDetails;
	public static final String MYPREFS2 = "mySharedPreferences";

	String selected_from = "";
	String selected_to = "";

	ArrayList<String> arrayList_from;
    ArrayList<String> arrayList_to;
	ArrayAdapter<String> arrayAdapter_from;
	ArrayAdapter<String> arrayAdapter_to;
	String uname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_do_transfer);

        // Get Server details from Shared Preference file.
        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
		serverip = serverDetails.getString("serverip", null);
		serverport = serverDetails.getString("serverport", null);

		Intent intent = getIntent();
		uname = intent.getStringExtra("uname");

		arrayList_from = new ArrayList<String>();
        arrayList_to = new ArrayList<String>();
        arrayAdapter_from = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_spinner_dropdown_item, arrayList_from);
        arrayAdapter_to = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_spinner_dropdown_item, arrayList_to);

		amount = (EditText) findViewById(R.id.editText_amount);
        from = (Spinner)findViewById(R.id.spinner);
        to = (Spinner)findViewById(R.id.spinner2);

		new RequestAccountTask().execute(uname);;

        from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getApplicationContext(),arraList.get(i)+"가 선택되었습니다.",
//                        Toast.LENGTH_SHORT).show();
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                String temp = arrayList_from.get(i);
                StringTokenizer st = new StringTokenizer(temp, "/");
                String str1="";
                str1 = st.nextToken();
                st = new StringTokenizer(str1," ");

                for(int k=0 ;  k < 3 ; k++) {
                    str1 = st.nextToken();
                }

                selected_from =str1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.BLACK);
				String temp = arrayList_to.get(i);
				StringTokenizer st = new StringTokenizer(temp, "/");
				String str2="";
				str2 = st.nextToken();
				st = new StringTokenizer(str2," ");

				for(int k=0 ;  k < 3 ; k++) {
					str2 = st.nextToken();
				}

				selected_to =str2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Handle the transfer functionality
		transfer = (Button) findViewById(R.id.button_Transfer);
		transfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Bundle extras = new Bundle();
				extras.putString("selected_from", selected_from);
				extras.putString("selected_to", selected_to);
				extras.putString("amount", amount.getText().toString());
				extras.putString("uname", uname);
				Intent aC = new Intent(getApplicationContext(), AccountConfirm.class);
				aC.putExtra("extras", extras);
				startActivity(aC);
			}
		});

	}

    @Override
    protected void onStart(){
        super.onStart();
		new RequestAccountTask().execute(uname);;
    }

    @Override
    protected void onResume(){
        super.onResume();
		new RequestAccountTask().execute(uname);
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
            String signal = "2";
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
                                arrayList_from.clear();
                                arrayList_to.clear();
                                JSONArray jsonArray= new JSONArray(result);

                                for(int i=0; i<jsonArray.length()-1; i++) {
                                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                                    Account_item account_item = new Account_item(jsonObject.getString("account_number"),
                                            String.valueOf(jsonObject.getInt("balance")), jsonObject.getString("username"));
                                    arrayList_to.add("계좌번호 : " + jsonObject.getString("account_number") + " / 계좌주인 :  " + jsonObject.getString("username"));
                                    if(jsonObject.getString("username").equals(uname)){
                                        arrayList_from.add("계좌번호 : " + jsonObject.getString("account_number") + " / 계좌주인 :  " + jsonObject.getString("username"));
                                    }
                                }

                                from.setAdapter(arrayAdapter_from);
                                to.setAdapter(arrayAdapter_to);

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

	/*
	The function that handles the aes256 decryption of the password from the encrypted password.
	password: Encrypted password input to the aes function
	returns: Plaintext password outputted by the aes function
	*/
	private String getNormalizedPassword(String password) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		CryptoClass crypt = new CryptoClass();
		return crypt.aesDeccryptedString(password);
	}


	public String convertStreamToString(InputStream in ) throws IOException {
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

		switch (item.getItemId()) {
			case android.R.id.home:
				//Write your logic here
				this.finish();
				return true;
			case R.id.action_settings:
				callPreferences();
				return true;

			case R.id.action_exit:
				Intent i = new Intent(getBaseContext(), LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}

	}

	public void callPreferences() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, FilePrefActivity.class);
		startActivity(i);
	}
}