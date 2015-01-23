package project.cis542.upenn.edu.alertreceiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SignUp extends Activity {
    public static int check_response = 0;
    private static String username;
    public static EditText ET_Username;
    public static EditText ET_Password;
    public static EditText ET_Confirm_Password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        ET_Username = (EditText) findViewById(R.id.username);
        ET_Password = (EditText) findViewById(R.id.password);
        ET_Confirm_Password = (EditText) findViewById(R.id.confirm_password);
        Button signup_button = (Button) findViewById(R.id.signup);
        signup_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if(ET_Username.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter a Username!!!", Toast.LENGTH_SHORT).show();
                }
                else if(ET_Password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Enter a Password!!!", Toast.LENGTH_SHORT).show();
                }
                else if(ET_Password.getText().toString().equals(ET_Confirm_Password.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Password Match!!!", Toast.LENGTH_SHORT).show();
                    CreateUser cu = new CreateUser();
                    cu.execute();
                    while(check_response == 0);
                    if(check_response == 1){
                        check_response = 0;
                        //username = ET_Username.getText().toString();
                        Intent intent = new Intent(getApplicationContext(), SelectApp.class);
                        intent.putExtra("username", ET_Username.getText().toString());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    if(check_response == 2){
                        check_response = 0;
                        Toast.makeText(getApplicationContext(), "Error While Creating User! Please try Again.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), SignUp.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    if(check_response == 3){
                        check_response = 0;
                        Toast.makeText(getApplicationContext(), "Database Error. Please try again!!!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), SignUp.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Password MisMatch!!!Please try Again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SignUp.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            } });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public class CreateUser extends AsyncTask {
        @SuppressLint("NewApi")
        public void readAndParseJSON(String in) {
            try {
                JSONObject reader = new JSONObject(in);
                Log.v("Checking Print",in);
                if(reader.getInt("DBConnection") == 1){
                    if(reader.getInt("UserCreation") == 1){
                        SignUp.check_response = 1;
                    }
                    else{
                        SignUp.check_response = 2;
                    }
                }
                else{
                    SignUp.check_response = 3;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        protected Object doInBackground(Object[] objects)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/createUser.php");
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("username", SignUp.ET_Username.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("password", SignUp.ET_Password.getText().toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //httpclient.execute(httppost);
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                Log.e("Error",response.toString());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream stream = entity.getContent();
                    String data = convertStreamToString(stream);
                    readAndParseJSON(data);
                    stream.close();
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        public String convertStreamToString(java.io.InputStream is) {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}
