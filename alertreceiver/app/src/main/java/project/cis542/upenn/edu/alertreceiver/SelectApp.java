package project.cis542.upenn.edu.alertreceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class SelectApp extends Activity {
    private static boolean check_connection;
    private static final int CHECK_DIALOG = 1;
    private static String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectapp);
        username = getIntent().getExtras().getString("username");
        check_connection = CheckInternet(this);
//        if (check_connection){
//            Toast.makeText(this, "Internet Connection Available!!!", Toast.LENGTH_LONG).show();
//        }
//        else{
//            //Toast.makeText(this, "Internet Connection Not Available!!!", Toast.LENGTH_SHORT).show();
//            removeDialog(CHECK_DIALOG);
//            showDialog(CHECK_DIALOG);
//        }
        Button sensor_button = (Button) findViewById(R.id.sensor);
        sensor_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                stopService(new Intent(getApplicationContext(), SensorService.class));
                SharedPreferences save = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = save.edit();
                editor.putString("user", username);
                editor.commit();
                stopService(new Intent(getApplicationContext(), SensorService.class));
                Intent intent=new Intent(getApplicationContext(), SensorService.class);
                //intent.putExtra("username",username);
                startService(intent);
                Intent intent2 = new Intent(getApplicationContext(), MainActivitySensor.class);
                //intent2.putExtra("username",username);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
            } });
        Button receiver_button = (Button) findViewById(R.id.receiver);
        receiver_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                stopService(new Intent(getApplicationContext(), HandleJSON.class));
                SharedPreferences save = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = save.edit();
                editor.putString("user", username);
                editor.commit();
                Intent intent=new Intent(getApplicationContext(), HandleJSON.class);
                //intent.putExtra("username",username);
                startService(intent);
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                //intent2.putExtra("username",username);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
            } });
    }
    //Function to check the Internet Connection Status
    public boolean CheckInternet(Context ctx) {
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
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
    public void onBackPressed() {
        Intent score = new Intent(getApplicationContext(), StartApp.class);
        score.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(score);
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
}
