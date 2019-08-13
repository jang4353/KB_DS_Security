package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpConnection;
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TalklistDetail extends Activity {

    private Intent intent;
    TextView title_view;
    TextView content_view;
    TextView date_view;
    TextView author_view;
    TextView file_view;
    ProgressBar loading_bar;

    Button delete_button;
    String uname;
    String author;
    Bundle extras;
    JSONObject jsonObject = new JSONObject();
    String result;
    BufferedReader reader;
    String serverip = "";
    String serverport = "";
    String protocol = "http://";
    SharedPreferences serverDetails;
    String title;
    String content;
    String date;
    String file;
    String filename="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talklist_detail);

        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        intent = getIntent();
        extras = getIntent().getBundleExtra("extras");

        author = extras.getString("username");
        uname = extras.getString("uname");
        title = extras.getString("title");
        content = extras.getString("content");
        date = extras.getString("date");

        new TalklistDetail.RequestDetailTalklistTask().execute(uname);

        title_view = (TextView)findViewById(R.id.title_view);
        content_view = (TextView)findViewById(R.id.content_view);
        date_view = (TextView)findViewById(R.id.date_view1);
        author_view = (TextView)findViewById(R.id.author_view1);
        file_view = (TextView)findViewById(R.id.file_view);
        loading_bar = (ProgressBar)findViewById(R.id.upload_bar);


        title_view.setText("제목 : " + title);
        content_view.setText("내용 : " + content);
        date_view.setText("작성일 : " + date);
        author_view.setText("작성자 : " + author);

            file_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TextView 클릭될 시 할 코드작성
                    loading_bar.setVisibility(View.VISIBLE);
                    new TalklistDetail.RequestFileTransferTask().execute(uname);

                }
            });

        delete_button = (Button)findViewById(R.id.talklist_delete);
        delete_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new TalklistDetail.RequestDeleteTalklistTask().execute(uname);

            }
        });

    }


    class RequestFileTransferTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/files");
            List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >(2);

            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("filename", filename));

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
                        if (result.indexOf("Download Successful") != -1) {
                            //	Below code handles the Json response parsing
                            JSONObject jsonObject;

                            try {
                                jsonObject = new JSONObject(result);

                                String download_file = jsonObject.getString("file");
                                saveImage(getApplicationContext(),download_file);

                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });

        }

        public  File saveImage(final Context context, final String imageData) throws IOException {
            final byte[] imgBytesData = android.util.Base64.decode(imageData,
                    android.util.Base64.DEFAULT);

            loading_bar.setVisibility(View.GONE);

            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ "/" + filename);

            final FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
                return null;
            }

            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fileOutputStream);
            try {
                bufferedOutputStream.write(imgBytesData);
                Toast.makeText(getApplicationContext(),  "Download Successful", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
                return null;
            } finally {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
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

    class RequestDetailTalklistTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/detailtalklist");
            List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >(5);

            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("compare_username", author));
            nameValuePairs.add(new BasicNameValuePair("title", title));
            nameValuePairs.add(new BasicNameValuePair("content", content));
            nameValuePairs.add(new BasicNameValuePair("date", date));

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
                        if (result.indexOf("Detail Successful") != -1) {
                            //	Below code handles the Json response parsing
                            JSONObject jsonObject;

                            try {
                                jsonObject = new JSONObject(result);

                                if((filename = jsonObject.getString("filename")).equals("")) {
                                    file_view.setVisibility(View.INVISIBLE);
                                }
                                else file_view.setText("업로드 한 파일 : \n" + filename);


                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),  "File Upload Fail", Toast.LENGTH_LONG).show();
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

    class RequestDeleteTalklistTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/deletetalklist");
            List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >(5);

            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("compare_username", author));
            nameValuePairs.add(new BasicNameValuePair("title", title));
            nameValuePairs.add(new BasicNameValuePair("content", content));
            nameValuePairs.add(new BasicNameValuePair("date", date));

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
                        if (result.indexOf("Delete Successful") != -1) {
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
                            Toast.makeText(getApplicationContext(),  "Delete Fail", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),  "Delete Fail", Toast.LENGTH_LONG).show();
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
