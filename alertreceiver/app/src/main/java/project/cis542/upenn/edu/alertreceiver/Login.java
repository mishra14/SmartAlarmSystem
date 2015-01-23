package project.cis542.upenn.edu.alertreceiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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


public class Login extends Activity {
    private static final int CHECK_DIALOG = 1;
    public static boolean check_user = false;
    public static int validate_response = 0;
    public static EditText entered_user;
    public static EditText entered_pass;
    private static String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        entered_user = (EditText) findViewById(R.id.username);
        entered_pass = (EditText) findViewById(R.id.password);
        Button login_button = (Button) findViewById(R.id.login);
        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                ValidateUser vu = new ValidateUser();
                vu.execute();
                while(validate_response == 0);
                if(validate_response == 1){
                    validate_response = 0;
                    //Toast.makeText(getApplicationContext(), "User Available!!!", Toast.LENGTH_LONG).show();
                    username = entered_user.getText().toString();
                    Intent intent = new Intent(getApplicationContext(), SelectApp.class);
                    intent.putExtra("username", username);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    entered_user.setText(null);
                    entered_pass.setText(null);
                }
                if(validate_response == 2){
                    validate_response = 0;
                    Toast.makeText(getApplicationContext(), "Incorrect Username or Password! Please try Again.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                if(validate_response == 3){
                    validate_response = 0;
                    Toast.makeText(getApplicationContext(), "Database Error. Please try again!!!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } });

    }
    //Function to Handle the Dialogs
    protected Dialog onCreateDialog(int id) {
        if (id == CHECK_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // this is the message to display
            builder.setMessage("Internet Connection not Available! Please enable Wi-Fi or Mobile Data to use this app.");
            builder.setCancelable(false);
            // this is the button to display
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        // this is the method to call when the button is clicked
                        public void onClick(DialogInterface dialog, int id) {
                            // this will hide the dialog
                            dialog.cancel();
                            System.exit(0);
                        }
                    });
            return builder.create();
        }
        else return null;
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
    public class ValidateUser extends AsyncTask{
        @SuppressLint("NewApi")
        public void readAndParseJSON(String in) {
            try {
                JSONObject reader = new JSONObject(in);
                if(reader.getInt("DBConnection") == 1){
                    if(reader.getInt("validate") == 1){
                        Login.check_user = true;
                        Login.validate_response = 1;
                    }
                    else{
                        //Toast.makeText(Login.login_context, "User Not Available!!!", Toast.LENGTH_LONG).show();
                        Login.check_user = false;
                        Login.validate_response = 2;
                    }
                }
                else{
                    Login.check_user = false;
                    Login.validate_response = 3;
                    //Toast.makeText(getApplicationContext(), "Error In Database Server!!!", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        protected Object doInBackground(Object[] objects)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/validateUser.php");
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("username", Login.entered_user.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("password", Login.entered_pass.getText().toString()));
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
