package com.android.insecurebankv2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

public class AddTalklist extends Activity {

    EditText title_add;
    EditText content_add;
    TextView date_view;
    TextView author_view;
    TextView upload_text;
    Button write_button;
    Button upload_button;
    String uname;
    File tempFile;
    Bitmap image_bitmap;
    String name_Str;
    String file_path;
    String file_data;

    long now = System.currentTimeMillis();
    Date date = new Date();
    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    JSONObject jsonObject = new JSONObject();
    String result;
    BufferedReader reader;
    String serverip = "";
    String serverport = "";
    String protocol = "http://";
    SharedPreferences serverDetails;
    Intent intent;
    String ba1;
    String signal;

    final int REQ_CODE_SELECT_IMAGE=100;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_talklist);

        title_add = (EditText)findViewById(R.id.title_add);
        content_add = (EditText)findViewById(R.id.content_add);
        date_view = (TextView)findViewById(R.id.date_view);
        author_view = (TextView)findViewById(R.id.author_view);
        write_button = (Button) findViewById(R.id.talklist_add);
        upload_button = (Button)findViewById(R.id.file_upload);
        upload_text = (TextView)findViewById(R.id.upload_text);

        upload_button.setVisibility(View.VISIBLE);
        upload_text.setVisibility(View.INVISIBLE);

        // Get Server details from Shared Preference file.
        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
        serverip = serverDetails.getString("serverip", null);
        serverport = serverDetails.getString("serverport", null);

        intent = getIntent();
        uname = intent.getStringExtra("uname");

        date_view.setText("작성일  : " + transFormat.format(date));
        author_view.setText("작성자 : " + uname);
        signal ="1";


        upload_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
//                intent = new Intent(Intent.ACTION_PICK);
//                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
//                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
                  intent = new Intent(Intent.ACTION_GET_CONTENT);
                  intent.setType("application/*");
                  intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                  //intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                  startActivityForResult(intent,1);
            }
        });

        write_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new RequestAddTalklistTask().execute(uname);
            }
        });
    }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        signal="1";
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();

//        if(requestCode == REQ_CODE_SELECT_IMAGE)
//        {
//            if(resultCode==Activity.RESULT_OK)
//            {
//        switch (requestCode){
//            case 1:
//                if(resultCode==RESULT_OK){
//                    String name_Str = data.getData().getPath();
//                }
//                break;
//        }
//                try {
                    //Uri에서 이미지 이름을 얻어온다.
//                    name_Str = getImageNameToUri(data.getData());
//
//                    //이미지 데이터를 비트맵으로 받아온다.
//                    image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    file_path = data.getData().getPath();
                    name_Str = file_path.substring(file_path.lastIndexOf("/")+1);
                    upload_button.setVisibility(View.INVISIBLE);
                    upload_text.setVisibility(View.VISIBLE);
                    upload_text.setText("업로드된 파일 : \n" + name_Str);
                    //Toast.makeText(getBaseContext(), "resultCode : "+data.getData().getPath(),Toast.LENGTH_SHORT).show();
                    signal="2";

//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
//
//                        } else {
//                            ActivityCompat.requestPermissions(this,
//                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                    1);
//                        }
//                    }

                    //Log.e("DONGUK", image_bitmap.toString());
                    tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + name_Str);
                     Log.e("DONGUK", "" + tempFile);

                    if(!tempFile.exists()){
                        Toast.makeText(getBaseContext(), "파일이 존재하지 않음",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.e("DONGUK", "" + tempFile);
                    file_data = fileToString(tempFile);
                    Uri tempUri = Uri.fromFile(tempFile);

                    intent.putExtra("crop", "true");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);

                    //intent.setType("image/*");
                    //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();


//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public String fileToString(File file)  {

        Log.e("DONGUK",""+file);
        FileInputStream inputStream =  null;
        ByteArrayOutputStream byteOutStream = null;

        byte[] fileArray = null;

        try {
            inputStream = new FileInputStream(file);
            byteOutStream = new ByteArrayOutputStream();

            Log.e("DONGUK",""+inputStream);

            int len = 0;

            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf)) != -1) {
                byteOutStream.write(buf, 0, len);
            }

            fileArray = byteOutStream.toByteArray();
            Log.e("DONGUK",""+byteOutStream);
            byteOutStream.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return Base64.encodeToString(fileArray, Base64.DEFAULT);

    }

    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }


    class RequestAddTalklistTask extends AsyncTask< String, String, String > {

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
            HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/addtalklist");
            List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > (7);


            if(signal.equals("2")) {
                Log.e("DONGUK", "KKK1");
//                ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                image_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
//                byte[] ba = bao.toByteArray();
//                ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
//                StringBuffer buffer = new StringBuffer();
//                FileInputStream fis = openFileInput(file_path);
//                BufferedReader  reader = new BufferedReader(new InputStreamReader(fis));
//                String str = reader.readLine();



            }


            now = System.currentTimeMillis();
            date = new Date();
            transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            nameValuePairs.add(new BasicNameValuePair("username", uname));
            nameValuePairs.add(new BasicNameValuePair("title", URLEncoder.encode(title_add.getText().toString(),"utf-8")));
            nameValuePairs.add(new BasicNameValuePair("content", content_add.getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("date", transFormat.format(date)));
            nameValuePairs.add(new BasicNameValuePair("signal",signal));
            if(signal.equals("2")) {
                nameValuePairs.add(new BasicNameValuePair("file", fileToString(new File(Environment.getExternalStorageDirectory() + "/" + name_Str))));
                nameValuePairs.add(new BasicNameValuePair("filename", name_Str));
            }
            else{
                nameValuePairs.add(new BasicNameValuePair("file", ""));
                nameValuePairs.add(new BasicNameValuePair("filename", ""));
            }

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
                            if (result.indexOf("Write Successful") != -1) {
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
                                Toast.makeText(getApplicationContext(),  "Write Fail", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),  "Write Fail", Toast.LENGTH_LONG).show();
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
