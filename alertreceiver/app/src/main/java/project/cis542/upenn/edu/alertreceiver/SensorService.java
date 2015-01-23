package project.cis542.upenn.edu.alertreceiver;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankit on 12/13/2014.
 */
public class SensorService extends Service implements SensorEventListener
{
    private static final String TAG = "SensorService";
    private static final String SensorValues = "SensorValues";
    private static final String ErrorCode = "ErrorCode";
    private static final String commType = "commType";
    private static String username="";
    private static final int audioSampleCount=10;
    private static SensorSet sensorSet;
    private static boolean hasMIC;
    private static boolean hasLight;
    private static boolean hasMotion;
    private static SensorManager sensorManager;
    private static Sensor lightSensor;
    private static Sensor linearAccelerationSensor;
    private static Handler fetchSoundHandler;
    private static Handler broadcastDataHandler;
    private static int broadcastDuration=2000;          //time gap between 2 broadcasts in msec
    private static int status;
    public static Context context;
    private int rawLevelBattery;
    private final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent intent)
        {
            rawLevelBattery = intent.getIntExtra("level", -1);
        }
    };
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        // TODO Auto-generated method stub
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            sensorSet.setLightLevel(Double.parseDouble(new DecimalFormat("#.##").format(sensorEvent.values[0])));
        }
        else if (sensorEvent.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION)
        {
            sensorSet.setX(Double.parseDouble(new DecimalFormat("#.##").format(sensorEvent.values[0])));
            sensorSet.setY(Double.parseDouble(new DecimalFormat("#.##").format(sensorEvent.values[1])));
            sensorSet.setZ(Double.parseDouble(new DecimalFormat("#.##").format(sensorEvent.values[2])));
        }
        //Log.v("OnSensorChange", sensorSet.toString());
        //broadcastSensorData(true,0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    Runnable fetchSoundData = new Runnable()
    {
        @Override
        public void run()
        {
            SoundSensor soundSensor=new SoundSensor();
            double micAmplitudeAvg=0.0;
            if(!soundSensor.start())
            {
                Log.e("Error","Error in starting the MIC");
            }
            else
            {
                for(int i=0;i<audioSampleCount;i++)                     //TODO check these again
                {
                    micAmplitudeAvg+=soundSensor.getAmplitudeDB();
                    //Log.v("Sound level", Double.toString(micAmplitude));
                    try
                    {
                        Thread.sleep(50,0);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e("thread",e.toString());
                    }
                }
                sensorSet.setMicLevel(Double.parseDouble(new DecimalFormat("#.##").format(micAmplitudeAvg/audioSampleCount)));
                //Log.v("OnSoundChange", sensorSet.toString());
                soundSensor.stop();
                soundSensor.reset();
                soundSensor.release();
                //broadcastSensorData(true,0);
                fetchSoundHandler.postDelayed(fetchSoundData, 500);
            }

        }
    };

    Runnable broadcastData = new Runnable()
    {
        @Override
        public void run()
        {
            if(!checkInternet(getApplicationContext()))
            {
                broadcastSensorData(false,3);
            }
            else
            {
                broadcastSensorData(true, 0);
            }
            broadcastDataHandler.postDelayed(broadcastData, broadcastDuration);
        }
    };



    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void onCreate()
    {
        Log.v(TAG, "Background Sensor Service Created");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = sharedPreferences.getString("user","mishra14");
        PackageManager pm = getPackageManager();
        // code to start mic related stuff
        hasMIC = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        if (hasMIC)
        {
            fetchSoundHandler = new Handler();
            fetchSoundHandler.post(fetchSoundData);
        }
        else
        {
            //TODO code if no mic
        }
        //code for the sensors
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
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }
        if(linearAccelerationSensor==null)
        {
            hasMotion=false;
            //TODO set code if no motion sensor present
        }
        else
        {
            hasMotion=true;
            sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        broadcastDataHandler=new Handler();
        broadcastDataHandler.postDelayed(broadcastData,1000);
    }

    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Background Sensor Service Stopped", Toast.LENGTH_LONG).show();
        Log.v(TAG, "Background Sensor Service Stopped");
        if(hasLight || hasMotion)
        {
            sensorManager.unregisterListener(this);
        }
        if(hasMIC)
        {
            fetchSoundHandler.removeCallbacksAndMessages(null);
        }
        this.unregisterReceiver(this.batteryLevelReceiver);
        broadcastDataHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Background Sensor Service Started", Toast.LENGTH_LONG).show();
        Log.v(TAG, "Background Sensor Service Started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
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
    public void broadcastSensorData(boolean isData, int errorCode)
    {
        Intent returnResultsIntent = new Intent("FETCH_MIC_VALUES");
        if(isData)
        {
            sendToServer sendSensorData = new sendToServer();
            sendSensorData.execute();
            returnResultsIntent.putExtra(commType, "Data");
            returnResultsIntent.putExtra(SensorValues, sensorSet.toString());
            returnResultsIntent.putExtra("Battery", rawLevelBattery);
            returnResultsIntent.putExtra("Status",status);
        }
        else
        {
            returnResultsIntent.putExtra(commType, "Error");
            returnResultsIntent.putExtra(ErrorCode, errorCode);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(returnResultsIntent);
    }


    class sendToServer extends AsyncTask
    {
        protected Object doInBackground(Object[] objects)
        {
            Log.v("AsyncTask ","executing async task");
            int storeSensorResponse=0;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mishra14.ddns.net/storeSensor.php");
            try
            {
                List<NameValuePair> keyValuePairs = new ArrayList<NameValuePair>(7);
                keyValuePairs.add(new BasicNameValuePair("username",username));
                keyValuePairs.add(new BasicNameValuePair("light",Double.toString(sensorSet.getLightLevel())));
                keyValuePairs.add(new BasicNameValuePair("sound",Double.toString(sensorSet.getMicLevel())));
                keyValuePairs.add(new BasicNameValuePair("motionX", Double.toString(sensorSet.getZ())));
                keyValuePairs.add(new BasicNameValuePair("motionY", Double.toString(sensorSet.getY())));
                keyValuePairs.add(new BasicNameValuePair("motionZ", Double.toString(sensorSet.getZ())));
                keyValuePairs.add(new BasicNameValuePair("battery",Double.toString(rawLevelBattery)));
                httppost.setEntity(new UrlEncodedFormEntity(keyValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity entity = httpResponse.getEntity();
                //Log.v("HTTP", entity.toString());
                if (entity != null)
                {
                    InputStream stream = entity.getContent();
                    String data = convertStreamToString(stream);
                    storeSensorResponse=parseJSON(data);
                    if(storeSensorResponse==1)
                    {
                        // all good
                        //do nothing
                        //broadcastSensorData(false, 2);
                    }
                    else if(storeSensorResponse==2)
                    {
                        //insertion failed
                        broadcastSensorData(false, storeSensorResponse);
                    }
                    else if (storeSensorResponse==0)
                    {
                        //DB connection failed
                        broadcastSensorData(false, storeSensorResponse);
                    }
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
            return storeSensorResponse;
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
                //Log.v("test",in);
                JSONObject responseJSON = new JSONObject(in);
                Log.v("Response JSON", responseJSON.toString());
                //Log.v("Response JSON", Integer.toString(responseJSON.getInt("DBConnection")));
                if(responseJSON.getInt("DBConnection")==1)
                {
                    //Log.v("Response JSON", "DB Connected");
                    status=responseJSON.getInt("SenseStatus");
                    if(responseJSON.getInt("SensorInsertion")==1)
                    {
                        //Log.v("Response JSON", "Sensor Insertion Complete");
                        // do nothing
                        return 1;
                    }
                    else
                    {
                        //.v("Response JSON", "Sensor Insertion Not complete");
                        //send an error broadcast to main activity
                        return 2;
                    }
                }
                else
                {
                    return 0;
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

}
