package project.cis542.upenn.edu.alertreceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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


public class MainActivity extends Activity {
    //URL to get JSON Object
    //private static String url = "http://mishra14.ddns.net/dummy.php";
    private static String url = "http://mishra14.ddns.net/fetchdata.php";
    private HandleJSON obj;
    private TextView timestamp;
    private TextView sound;
    private TextView light;
    private TextView temperature;
    private TextView motionX;
    private TextView motionY;
    private TextView motionZ;
    private TextView battery;
    private TextView timestamp_alert;
    private TextView sound_alert;
    private TextView light_alert;
    private TextView motion_alert;
    private TextView battery_alert;
    private Button button_accept;
    private Button button_ignore;
    private Button button_start;
    private Button button_stop;
    private Button button_quit;
    private Button button_exit;
    private NotificationManager mNotificationManager;
    private Handler displayDataHandler;
    private static String password="";
    private static boolean notifiedInternet=false;
    private static boolean outOfSync=false;
    private static boolean statusSync=true;
    private static String notificationText="";
    private static String notificationTitle="";
    private static int notificationType=0;
    private static int senseStatus=0;
    public static String username;
    private Handler updateStatus;
    private Handler notifyUser;
    private Context context;
    Runnable displayData = new Runnable() {
        @Override
        public void run() {
            //obj.execute();
            //obj.fetchJSON();
//            while(obj.parsingComplete);
            if (HandleJSON.errorData == true) {
                Toast.makeText(getApplicationContext(), "No Data is Present!Start the SensorApp First.", Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), HandleJSON.class));
                Intent score = new Intent(getApplicationContext(), SelectApp.class);
                score.putExtra("username", username);
                score.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(score);
            }
            else
            {
                Log.v("Status Receiver","server : "+HandleJSON.senseStatus+ " local : "+senseStatus);
                if(senseStatus!=HandleJSON.senseStatus)
                {
                    if(outOfSync)
                    {
                        Log.v("Status", "Syncing status with server");
                        senseStatus=HandleJSON.senseStatus;
                        updateStatus();
                        outOfSync=false;
                    }
                    outOfSync=true;

                }
                else
                {
//                    if(outOfSync)
//                    {
//                        statusSync=true;
//                        outOfSync=false;
//                    }
//                    else
//                    {
//
//                    }
                    outOfSync=false;
                }
                timestamp.setText(HandleJSON.getTimeStamp());
                sound.setText(HandleJSON.getSound());
                light.setText(HandleJSON.getLight());
                motionX.setText(HandleJSON.getMotionX());
                motionY.setText(HandleJSON.getMotionY());
                motionZ.setText(HandleJSON.getMotionZ());
                battery.setText(HandleJSON.getBattery());
                timestamp_alert.setText(HandleJSON.getTimestampAlert());
                sound_alert.setText(HandleJSON.getSoundAlert());
                light_alert.setText(HandleJSON.getLightAlert());
                motion_alert.setText(HandleJSON.getMotionAlert());
                battery_alert.setText(HandleJSON.getBatteryAlert());
                if(HandleJSON.alertStatus==1)
                {
                    findViewById(R.id.accept).setVisibility(View.VISIBLE);
                    findViewById(R.id.ignore).setVisibility(View.VISIBLE);
                }
                else
                {
                    findViewById(R.id.accept).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ignore).setVisibility(View.INVISIBLE);
                }
                displayDataHandler.postDelayed(displayData, 5000);
            }
        }
    };
    Runnable update = new Runnable()
    {
        @Override
        public void run()
        {
            updateStatus();
        }
    };
    Runnable notify = new Runnable()
    {
        @Override
        public void run()
        {
            if(notificationType==1)
            {
                notifyUser(notificationTitle,notificationText);
            }
            else
            {
                Toast.makeText(getApplicationContext(), notificationText, Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateStatus();
        context=this;
        //username = getIntent().getStringExtra("username");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sharedPreferences.getString("user","mishra14");

        button_accept = (Button) findViewById(R.id.accept);
        button_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Accept Button Pressed!", Toast.LENGTH_SHORT).show();
                if(!checkInternet(getApplicationContext()))
                {
                    Toast.makeText(getApplicationContext(), "Error : No Internet Connection", Toast.LENGTH_SHORT).show();
                    if(!notifiedInternet)
                    {
                        notifyUser("smart sensor", "Error : No Internet Connection");
                        notifiedInternet=true;
                    }
                }
                else
                {
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new AlertDialog.Builder(context)
                            .setTitle("Authentication ")
                            .setMessage("Enter password for " + username)
                            .setView(input)
                            .setCancelable(false)
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    password = input.getText().toString();
                                    new sendResponseToServer().execute();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    // Do nothing.
                                }
                            }).show();
                }
            }
        });

        button_ignore = (Button) findViewById(R.id.ignore);
        button_ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Ignore Button Pressed!", Toast.LENGTH_SHORT).show();
            }
        });

        button_start = (Button) findViewById(R.id.start);
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "StartAlert Button Pressed!", Toast.LENGTH_SHORT).show();
                if(!checkInternet(getApplicationContext()))
                {
                    Toast.makeText(getApplicationContext(), "Error : No Internet Connection", Toast.LENGTH_SHORT).show();
                    if(!notifiedInternet)
                    {
                        notifyUser("smart sensor", "Error : No Internet Connection");
                        notifiedInternet=true;
                    }
                }
                else
                {
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new AlertDialog.Builder(context)
                            .setTitle("Authentication ")
                            .setMessage("Enter password for " + username)
                            .setView(input)
                            .setCancelable(false)
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    password = input.getText().toString();
                                    new sendStatusToServer().execute();
                                    statusSync = false;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            }).show();
                }
            } });

        button_stop = (Button) findViewById(R.id.stop);
        button_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "StopAlert Button Pressed!", Toast.LENGTH_SHORT).show();
                if(!checkInternet(getApplicationContext()))
                {
                    Toast.makeText(getApplicationContext(), "Error : No Internet Connection", Toast.LENGTH_SHORT).show();
                    if(!notifiedInternet)
                    {
                        notifyUser("smart sensor", "Error : No Internet Connection");
                        notifiedInternet=true;
                    }
                }
                else
                {
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new AlertDialog.Builder(context)
                            .setTitle("Authentication ")
                            .setMessage("Enter password for " + username)
                            .setView(input)
                            .setCancelable(false)
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    password = input.getText().toString();
                                    new sendStatusToServer().execute();
                                    statusSync = false;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            }).show();
                }
            } });

        button_quit = (Button) findViewById(R.id.quit);
        button_quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                displayDataHandler.removeCallbacksAndMessages(null);
                updateStatus.removeCallbacksAndMessages(null);
                notifyUser.removeCallbacksAndMessages(null);
                Toast.makeText(getApplicationContext(), "Quit Button Pressed!", Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), HandleJSON.class));
                Intent intent = new Intent(getApplicationContext(), StartApp.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } });

        button_exit = (Button) findViewById(R.id.exit);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDataHandler.removeCallbacksAndMessages(null);
                updateStatus.removeCallbacksAndMessages(null);
                notifyUser.removeCallbacksAndMessages(null);
                Intent intent = new Intent(getApplicationContext(), StartApp.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });
        //Importing TextView
        timestamp = (TextView)findViewById(R.id.timestamp_value);
        sound = (TextView)findViewById(R.id.sensor_sound_value);
        light = (TextView)findViewById(R.id.sensor_light_value);
        //temperature = (TextView)findViewById(R.id.sensor_temp_value);
        motionX = (TextView)findViewById(R.id.sensor_motionX_value);
        motionY = (TextView)findViewById(R.id.sensor_motionY_value);
        motionZ = (TextView)findViewById(R.id.sensor_motionZ_value);
        battery = (TextView)findViewById(R.id.battery_value);
        timestamp_alert = (TextView)findViewById(R.id.timestamp_change);
        sound_alert = (TextView)findViewById(R.id.sensor_sound_change);
        light_alert = (TextView)findViewById(R.id.sensor_light_change);
        motion_alert = (TextView)findViewById(R.id.sensor_motionX_change);
        battery_alert = (TextView)findViewById(R.id.battery_change);
        //obj = new HandleJSON();
        //Intent intent=new Intent(this, HandleJSON.class);
        //intent.putExtra("username",username);
        //startService(intent);
        //this.registerReceiver(this.mBatInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        updateStatus=new Handler();
        notifyUser=new Handler();
        displayDataHandler=new Handler();
        displayDataHandler.post(displayData);
    }
    //Function to check the Internet Connection Status
    public boolean checkInternet(Context ctx) {
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
    }
    @Override
    public void onBackPressed()
    {
        displayDataHandler.removeCallbacksAndMessages(null);
        Intent score = new Intent(getApplicationContext(), SelectApp.class);
        score.putExtra("username",username);
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
    class sendResponseToServer extends AsyncTask
    {
        protected Object doInBackground(Object[] objects)
        {
            int senseUpdaterResponse=0;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/alertResponse.php");
            try
            {
                List<NameValuePair> keyValuePairs = new ArrayList<NameValuePair>(3);
                keyValuePairs.add(new BasicNameValuePair("username",username));
                keyValuePairs.add(new BasicNameValuePair("password",password));
                keyValuePairs.add(new BasicNameValuePair("alertResponse","1"));
                httppost.setEntity(new UrlEncodedFormEntity(keyValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity entity = httpResponse.getEntity();
                //Log.v("HTTP", entity.toString());
                if (entity != null)
                {
                    InputStream stream = entity.getContent();
                    String data = convertStreamToString(stream);
                    senseUpdaterResponse=parseJSON(data);
                    stream.close();
                }

            }
            catch (ClientProtocolException e)
            {
                // TODO Auto-generated catch block
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
            }
            return senseUpdaterResponse;
        }
        String convertStreamToString(java.io.InputStream is)
        {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        public int parseJSON(String in)
        {
            try
            {
                JSONObject responseJSON = new JSONObject(in);
                //Log.v("Response JSON", responseJSON.toString());
                //Log.v("Response JSON", Integer.toString(responseJSON.getInt("DBConnection")));
                if(responseJSON.getInt("DBConnection")==1)
                {
                    if(responseJSON.getInt("Validate")!=1)
                    {
                        statusSync=true;
                        notificationText="Invalid Password : Failed to update status";
                        notificationTitle="smart sensor";
                        notificationType=0;
                        notifyUser.post(notify);
                    }
                    else
                    {
                        if(responseJSON.getInt("AlertChanged")!=1)
                        {
                            statusSync=true;
                            notificationText="Error : failed to change alert status";
                            notificationTitle="smart sensor";
                            notificationType=0;
                            notifyUser.post(notify);
                        }

                    }
                }
                else
                {
                    statusSync=true;
                    //status unchanged
                    notificationText="Error : DB connection failed";
                    notificationTitle="smart sensor";
                    notificationType=0;
                    notifyUser.post(notify);
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return 0;
        }

    }
    class sendStatusToServer extends AsyncTask
    {
        protected Object doInBackground(Object[] objects)
        {
            int senseUpdaterResponse=0;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/senseStatusR.php");
            try
            {
                List<NameValuePair> keyValuePairs = new ArrayList<NameValuePair>(3);
                keyValuePairs.add(new BasicNameValuePair("username",username));
                keyValuePairs.add(new BasicNameValuePair("password",password));
                keyValuePairs.add(new BasicNameValuePair("status",(senseStatus==0?"ON":"OFF")));
                httppost.setEntity(new UrlEncodedFormEntity(keyValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity entity = httpResponse.getEntity();
                //Log.v("HTTP", entity.toString());
                if (entity != null)
                {
                    InputStream stream = entity.getContent();
                    String data = convertStreamToString(stream);
                    senseUpdaterResponse=parseJSON(data);
                    stream.close();
                }

            }
            catch (ClientProtocolException e)
            {
                // TODO Auto-generated catch block
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
            }
            return senseUpdaterResponse;
        }
        String convertStreamToString(java.io.InputStream is)
        {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        public int parseJSON(String in)
        {
            try
            {
                JSONObject responseJSON = new JSONObject(in);
                //Log.v("Response JSON", responseJSON.toString());
                //Log.v("Response JSON", Integer.toString(responseJSON.getInt("DBConnection")));
                if(responseJSON.getInt("DBConnection")==1)
                {
                    if(responseJSON.getInt("Update")!=1)
                    {
                        statusSync=true;
                        notificationText="Invalid Password : Failed to update status";
                        notificationTitle="smart sensor";
                        notificationType=0;
                        notifyUser.post(notify);
                    }
                    else
                    {
                        senseStatus = responseJSON.getInt("Status");
                        updateStatus.post(update);
                    }
                }
                else
                {
                    statusSync=true;
                    //status unchanged
                    notificationText="Error : DB connection failed";
                    notificationTitle="smart sensor";
                    notificationType=0;
                    notifyUser.post(notify);
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return 0;
        }

    }
    public void notifyUser(String title, String message)
    {
        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent resultPendingIntent =PendingIntent.getActivity(getApplicationContext(),0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(mNotificationId, notificationBuilder.build());
    }
    public void updateStatus()
    {
        if(senseStatus==1)
        {
            findViewById(R.id.start).setVisibility(View.INVISIBLE);
            findViewById(R.id.stop).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.start).setVisibility(View.VISIBLE);
            findViewById(R.id.stop).setVisibility(View.INVISIBLE);
        }
    }

}
