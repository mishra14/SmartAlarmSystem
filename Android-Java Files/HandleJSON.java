package project.cis542.upenn.edu.alertreceiver;

/**
 * Created by Jitesh on 28-11-2014.
 */
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//import android.support.v4.app.NotificationCompat;

public class HandleJSON extends Service {
    public static String timestamp = "timestamp";
    public static String sound = "sound";
    public static String light = "light";
    public static String motionX = "motionX";
    public static String motionY = "motionY";
    public static String motionZ = "motionZ";
    public static String battery = "battery";
    public static String timestamp_alert = "--";
    public static String sound_alert = "--";
    public static String light_alert = "--";
    public static String motion_alert = "--";
    public static String battery_alert = "--";
    public static String reason;
    public static String status;
    public static int senseStatus;
    public static int alertStatus=0;
    private static String username = null;
    private NotificationManager mNotificationManager;
    private Handler fetchDataHandler;
    //public String temperature = "temperature";
    //JSON Node Names
    private static final String Sensor_Data = "Data";
    private static final String Sensor_TimeStamp = "timestamp";
    private static final String Sensor_Light = "light";
    private static final String Sensor_Sound = "sound";
    private static final String Sensor_MotionX = "motionX";
    private static final String Sensor_MotionY = "motionY";
    private static final String Sensor_MotionZ = "motionZ";
    private static final String Sensor_Battery = "battery";
    private static final String Sensor_Error = "Error";
    private static final String Alert = "Alert";
    private static final String AlertData = "AlertData";
    private static final String Status = "status";
    private static final String Reason = "reason";
    private static final String TimeStampAlert = "timestampUpdate";
    private static final String MotionAlert = "MotionAlert";
    private static final String SoundAlert = "SoundAlert";
    private static final String LightAlert = "LightAlert";
    private static final String BatteryAlert = "BatteryAlert";
    private static final String SenseStatus="SenseStatus";
    //private static final String Sensor_Temperature = "temperature";

    public static volatile boolean parsingComplete = true;
    public static volatile boolean errorData = false;
    public static boolean notified = false;
    public static int sum = 0;
    public static int previous_sum = 0;
    public static String getTimeStamp() {
        return timestamp;
    }
    public static String getSound(){
        return sound;
    }
    public static String getLight(){
        return light;
    }
    public static String getMotionX(){
        return motionX;
    }
    public static String getMotionY(){
        return motionY;
    }
    public static String getMotionZ(){
        return motionZ;
    }
    public static String getBattery() {
        return battery;
    }
//    public String getTemperature(){
//        return temperature;
//    }
    public static String getTimestampAlert(){
        return timestamp_alert;
    }
    public static String getSoundAlert(){
        return sound_alert;
    }
    public static String getLightAlert(){
        return light_alert;
    }
    public static String getMotionAlert(){
        return motion_alert;
    }
    public static String getBatteryAlert(){
        return battery_alert;
    }
    Runnable fetchData=new Runnable()
    {
    @Override
        public void run()
        {
          boolean connection_available = CheckInternet(getApplicationContext());
        if (connection_available){
            new fetchDataNetworkClass().execute();
            fetchDataHandler.postDelayed(fetchData, 5000);
        }
        else{
            stopService(new Intent(getApplicationContext(), HandleJSON.class));
            String disconnect = "No Internet Connection!Timestamp-" + timestamp_alert;
            AlertNotification(disconnect);
        }

        }
    };
    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        Log.v("Check Print", in);
        errorData = false;
        try {
            JSONObject reader = new JSONObject(in);
            if(reader.getInt(Sensor_Error) == -1)
            {
                Log.v("test","here");
                errorData = true;
            }
            else{
                senseStatus=reader.getInt(SenseStatus);
                JSONObject sys = reader.getJSONObject(Sensor_Data);
                timestamp = sys.getString(Sensor_TimeStamp);
                sound = sys.getString(Sensor_Sound);
                light = sys.getString(Sensor_Light);
                motionX = sys.getString(Sensor_MotionX);
                motionY = sys.getString(Sensor_MotionY);
                motionZ = sys.getString(Sensor_MotionZ);
                battery = sys.getString(Sensor_Battery);
                //temperature = sys.getString(Sensor_Temperature);
            }
            alertStatus=reader.getInt(Alert);
            if(reader.getInt(Alert) == 1){
                if(reader.getInt(SoundAlert) == 1){
                    sound_alert = "Yes";
                }
                else{
                    sound_alert = "No";
                }
                if(reader.getInt(LightAlert) == 1){
                    light_alert = "Yes";
                }
                else{
                    light_alert = "No";
                }
                if(reader.getInt(MotionAlert) == 1){
                    motion_alert = "Yes";
                }
                else{
                    motion_alert = "No";
                }
                if(reader.getInt(BatteryAlert) == 1){
                    battery_alert = "Yes";
                }
                else{
                    battery_alert = "No";
                }
                sum = reader.getInt(BatteryAlert)*8 + reader.getInt(SoundAlert)*4 + reader.getInt(LightAlert)*2 + reader.getInt(MotionAlert)*1;
                Log.v("Sum",Integer.toString(sum));
                JSONObject sys = reader.getJSONObject(AlertData);
                timestamp_alert = sys.getString(TimeStampAlert);
                reason = sys.getString(Reason);
                status = sys.getString(Status);
                if(sum != previous_sum) {
                    String notify_user = "Alert! " + reason + " at " + timestamp_alert;
                    AlertNotification(notify_user);
                    previous_sum = sum;
                }
            }
            //parsingComplete = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    class fetchDataNetworkClass extends AsyncTask
    {
        protected Object doInBackground(Object[] objects)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/fetchdata.php");
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("username", username));
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
            try {
                URL url = new URL("http://jitesh.pythonanywhere.com/examples/email_test/sendemail?price=500&company=Apple&change=5");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                // TODO Auto-generated catch block
                Log.v("Checking Exception", "IOException");
            }

            return null;
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
        fetchDataHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        fetchDataHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sharedPreferences.getString("user","mishra14");
        fetchDataHandler.post(fetchData);
    }
    //Function to set Alert Notification
    public void AlertNotification(String reason){
        String subject = "SmartSensorAlert!!!";
        String body = reason;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify = new Notification(android.R.drawable.
                stat_notify_more, "Alert", System.currentTimeMillis());
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        //notificationIntent.putExtra("username",username);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                context, 0, notificationIntent, 0);
        notify.setLatestEventInfo(context, subject, body, pending);
        notify.defaults |= Notification.DEFAULT_SOUND;
        notify.defaults |= Notification.DEFAULT_VIBRATE;
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(0, notify);
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
}