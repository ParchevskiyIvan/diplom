package fly.speedmeter.grub;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class DataToBD {

    public String m_myDateTime;
    public String m_myTime;
    public String m_myMaxSpeed;
    public String m_myAvgSpeed;
    public String m_myDistance;
    public List<Float> m_myAccelX;
    public List<Float> m_myAccelY;

    public DataToBD() {
    }

    public DataToBD(String dateTime, String time, String maxSpeed, String avgSpeed, String distance, List<Float> accelX, List<Float> accelY) {
        this.m_myDateTime = dateTime;
        this.m_myTime = time;
        this.m_myMaxSpeed = maxSpeed;
        this.m_myAvgSpeed = avgSpeed;
        this.m_myDistance = distance;
        this.m_myAccelX = accelX;
        this.m_myAccelY = accelY;
    }
}
