package project.cis542.upenn.edu.alertreceiver;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Ankit on 11/24/2014.
 */
public class SoundSensor
{
    private MediaRecorder soundRecorder =null;
    private double REFERENCE =0.2;

    public SoundSensor()
    {

    }
    public boolean start()
    {
        if(soundRecorder==null)
        {
            soundRecorder=new MediaRecorder();
            soundRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            soundRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            soundRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            soundRecorder.setOutputFile("/dev/null");                               //file is not saved
            try
            {
                soundRecorder.prepare();
            }
            catch (IOException e)
            {
                Log.e("Error", "Mic recorder cannot be prepared; Exception : "+e.toString());
                return false;
            }
            catch (IllegalStateException e)
            {
                Log.e("Error", "Mic recorder is prepared after start() or before setOutputFormat(); Exception : "+e.toString());
                return false;
            }
            try
            {
                soundRecorder.start();
            }
            catch (IllegalStateException e)
            {
                Log.e("Error", "Mic recorder not started; Exception : "+e.toString());
                return false;
            }
        }
        return true;
    }
    public void stop()
    {
        try
        {
            soundRecorder.stop();
        }
        catch (IllegalStateException e)
        {
            Log.e("Error", "Mic recorder is stopped before starting; Exception : "+e.toString());
        }
        catch (RuntimeException e)
        {
            Log.e("Error", "Mic recorder is stopped just after starting; Exception : "+e.toString());
        }


    }
    public void release()
    {
        soundRecorder.release();
    }
    public void reset()
    {
        soundRecorder.reset();
    }
    public double getAmplitude()
    {
        if(soundRecorder==null)
        {
            Log.e("Error", "sound recorder not created");
            return 0.0;
        }
        else
        {
            return soundRecorder.getMaxAmplitude();
        }
    }
    public double getAmplitudeDB()
    {
        double amp=getAmplitude();
        if(amp==0.0)
        {
            return 0.0;
        }
        else
        {
            return ((20*Math.log10(amp/(REFERENCE))));        //TODO - get the math right
        }
    }
}
