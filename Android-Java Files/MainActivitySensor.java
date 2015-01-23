package project.cis542.upenn.edu.alertreceiver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivitySensor extends Activity
{
    private TextView textView;
    private static final String SensorValues = "SensorValues";
    private static final String ErrorCode="ErrorCode";
    private static final String commType="commType";
    private static boolean hasMIC;
    private static boolean hasLight;
    private static boolean hasMotion;
    private static SensorSet sensorSet;
    Handler updateStatus;
    Handler notifyUser;
    Handler sendStatusRequest;
    private static SensorManager sensorManager;
    private static Sensor lightSensor;
    private static Sensor linearAccelerationSensor;
    public static Context context;
    private static int  senseStatus=0;
    private static int batteryLevel;
    private static boolean statusSync=true;
    private static boolean outOfSync=false;
    private static boolean sendStatusRequestFlag=false;
    private static SensorDataReceiver sensorDataReceiver;
    private static String notificationTitle="";
    private static String notificationText="";
    private static int notificationType=0;
    private static String username;
    private static String password;
    private boolean notifiedInternet=false;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_sensor);
        context = this;                 //store the activity context for later use
        //get sensor availability
        PackageManager pm = getPackageManager();
        hasMIC = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        linearAccelerationSensor =sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorSet=new SensorSet();
        if (lightSensor == null)
        {
            hasLight=false;
            //TODO code if no light sensor
        }
        else
        {
            hasLight=true;
        }
        if(linearAccelerationSensor==null)
        {
            hasMotion=false;
            //TODO set code if no motion sensor present
        }
        else
        {
            hasMotion=true;
        }
        //TODO check sensor availability and alert user
        updateStatus=new Handler();
        notifyUser=new Handler();
        sendStatusRequest=new Handler();
        sensorDataReceiver=new SensorDataReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver( sensorDataReceiver, new IntentFilter("FETCH_MIC_VALUES"));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sharedPreferences.getString("user","mishra14");
        //stopService(new Intent(getApplicationContext(), SensorService.class));
        //startService(new Intent(this, SensorService.class));
        updateStatus();
    }

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
    //Action Bar menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy()
    {
        updateStatus.removeCallbacksAndMessages(null);
        notifyUser.removeCallbacksAndMessages(null);
        sendStatusRequest.removeCallbacksAndMessages(null);
        //this.unregisterReceiver(this.sensorDataReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
    //Function to check the Internet Connection Status
    public boolean checkInternet(Context ctx)
    {
        ConnectivityManager conn = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return wifi.isConnected() || mobile.isConnected();
    }
    public void notifyUser(String title, String message)
    {
        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =PendingIntent.getActivity(context,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
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
            findViewById(R.id.startButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.startButton).setVisibility(View.VISIBLE);
            findViewById(R.id.stopButton).setVisibility(View.INVISIBLE);
        }
    }

    public class SensorDataReceiver extends BroadcastReceiver
    {
        private boolean notified=false;
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getStringExtra(commType).equals("Data"))
            {
                sensorSet = new SensorSet(intent.getStringExtra(SensorValues));
                int batteryLevel = intent.getIntExtra("Battery",69);
                textView = (TextView) findViewById(R.id.soundSensor);
                textView.setText("Sound Level : " + Double.toString(sensorSet.getMicLevel()) + " dB");
                textView = (TextView) findViewById(R.id.lightSensor);
                textView.setText("Light Level : " + Double.toString(sensorSet.getLightLevel()) + " lux");
                textView = (TextView) findViewById(R.id.accSensor);
                textView.setText("Motion : \nx = " + Double.toString(sensorSet.getX()) + " m/s/s\ny = " + Double.toString(sensorSet.getY()) + " m/s/ss\nz = " + Double.toString(sensorSet.getZ()) + " m/s/s");
                textView = (TextView) findViewById(R.id.battery);
                textView.setText("Battery : " + batteryLevel + " %");
                Log.v("Status","server : "+intent.getIntExtra("Status",0)+ " local : "+senseStatus);
                if(senseStatus!=intent.getIntExtra("Status",0))
                {
                    if(outOfSync)
                    {
                        Log.v("Status", "Syncing status with server");
                        senseStatus=intent.getIntExtra("Status",0);
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
            }
            else
            {
                int storeSensorResponseErrorCode=intent.getIntExtra(ErrorCode,4);
                if(storeSensorResponseErrorCode==1)
                {
                    //all good
                }
                else if(storeSensorResponseErrorCode==2)
                {
                    //insertion failed
                    Toast.makeText(getApplicationContext(), "Error : Insertion into DB failed", Toast.LENGTH_SHORT).show();
                    if(!notified)
                    {
                        notifyUser("smart sensor", "Error : Insertion into DB failed");
                        notified=true;
                    }

                }
                else if (storeSensorResponseErrorCode==0)
                {
                    //db failed to connect
                    Toast.makeText(getApplicationContext(), "Error : DB connection failed in uploading data", Toast.LENGTH_SHORT).show();
                    if(!notified)
                    {
                        notifyUser("smart sensor", "Error : DB connection failed in uploading data");
                        notified=true;
                    }
                }
                else if (storeSensorResponseErrorCode==3)
                {
                    //db failed to connect
                    Toast.makeText(getApplicationContext(), "Error : No Internet Connection", Toast.LENGTH_SHORT).show();
                    if(!notified)
                    {
                        notifyUser("smart sensor", "Error : No Internet Connection");
                        notified=true;
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Error : Unknown error code - "+storeSensorResponseErrorCode, Toast.LENGTH_SHORT).show();
                    if(!notified)
                    {
                        notifyUser("smart sensor", "Error : Unknown error code - "+storeSensorResponseErrorCode);
                        notified=true;
                    }
                }
            }
        }
    }
    class sendStatusToServer extends AsyncTask
    {
        protected Object doInBackground(Object[] objects)
        {
            int senseUpdaterResponse=0;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/senseStatus.php");
            try
            {
                List<NameValuePair> keyValuePairs = new ArrayList<NameValuePair>(8);
                keyValuePairs.add(new BasicNameValuePair("username",username));
                keyValuePairs.add(new BasicNameValuePair("password",password));
                keyValuePairs.add(new BasicNameValuePair("light",Double.toString(sensorSet.getLightLevel())));
                keyValuePairs.add(new BasicNameValuePair("sound",Double.toString(sensorSet.getMicLevel())));
                keyValuePairs.add(new BasicNameValuePair("motionX", Double.toString(sensorSet.getZ())));
                keyValuePairs.add(new BasicNameValuePair("motionY", Double.toString(sensorSet.getY())));
                keyValuePairs.add(new BasicNameValuePair("motionZ", Double.toString(sensorSet.getZ())));
                keyValuePairs.add(new BasicNameValuePair("battery", Double.toString(batteryLevel)));
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
                    notificationText="Error : DB connection failed in status update";
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void startDetection(View view)
    {
        //TODO put correct logic on start detection
        //sendStatusRequest.post(sendStatus);
        //new sendStatusToServer().execute();
        //statusSync=false;
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
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
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
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void  stopDetection(View view)
    {
        //TODO put correct protection for stopping
        //sendStatusRequest.post(sendStatus);
        //new sendStatusToServer().execute();
        //statusSync=false;
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
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            new AlertDialog.Builder(this)
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
    }
    public void quitApplication(View view)
    {
        updateStatus.removeCallbacksAndMessages(null);
        notifyUser.removeCallbacksAndMessages(null);
        sendStatusRequest.removeCallbacksAndMessages(null);
        //this.unregisterReceiver(this.sensorDataReceiver);
        Toast.makeText(getApplicationContext(), "Quit Button Pressed!", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplicationContext(), SensorService.class));
        Intent intent = new Intent(getApplicationContext(), StartApp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //onDestroy();
        //System.exit(0);
    }
    public void onBackPressed()
    {
        //do nothing
        //back button disabled
    }
    public void exit(View view)
    {
        updateStatus.removeCallbacksAndMessages(null);
        notifyUser.removeCallbacksAndMessages(null);
        sendStatusRequest.removeCallbacksAndMessages(null);
        //this.unregisterReceiver(this.sensorDataReceiver);
        Intent intent = new Intent(getApplicationContext(), StartApp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }
}


