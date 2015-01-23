package project.cis542.upenn.edu.alertreceiver;

/**
 * Created by Ankit on 12/15/2014.
 */
public class SensorSet
{
    private double micLevel;
    private double x;
    private double y;
    private double z;
    private double lightLevel;

    public SensorSet(double micLevel, double x, double y, double z, double lightLevel)
    {
        this.micLevel = micLevel;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lightLevel = lightLevel;
    }
    public SensorSet(SensorSet sensorSet)
    {
        this.micLevel = sensorSet.getMicLevel();
        this.x = sensorSet.getX();
        this.y = sensorSet.getY();
        this.z = sensorSet.getZ();
        this.lightLevel = sensorSet.getLightLevel();
    }
    public SensorSet(String str)
    {
        this.micLevel = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.lightLevel = 0;
        String delimiter ="[=,]";
        String[] tokens =str.split(delimiter);
        for (int i=0;i<tokens.length;i++)
        {
            //Log.v("Sensor", tokens[i]);
            if(tokens[i].equals("micLevel"))
            {
                this.micLevel=Double.valueOf(tokens[i+1]);
            }
            else if(tokens[i].equals("x"))
            {
                this.x=Double.valueOf(tokens[i+1]);
            }
            else if(tokens[i].equals("y"))
            {
                this.y=Double.valueOf(tokens[i+1]);
            }
            else if(tokens[i].equals("z"))
            {
                this.z=Double.valueOf(tokens[i+1]);
            }
            else if(tokens[i].equals("lightLevel"))
            {
                this.lightLevel=Double.valueOf(tokens[i+1]);
            }
        }
        //Log.v("Sensor Object", this.toString());
    }
    public SensorSet()
    {
        this.micLevel = 0;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.lightLevel = 0;
    }

    @Override
    public String toString()
    {
        return  "micLevel=" + micLevel +
                ",x=" + x +
                ",y=" + y +
                ",z=" + z +
                ",lightLevel=" + lightLevel;
    }

    public double getMicLevel() {
        return micLevel;
    }

    public void setMicLevel(double micLevel) {
        this.micLevel = micLevel;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(double lightLevel) {
        this.lightLevel = lightLevel;
    }
}
