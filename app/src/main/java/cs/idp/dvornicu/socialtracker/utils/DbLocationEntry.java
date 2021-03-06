package cs.idp.dvornicu.socialtracker.utils;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DbLocationEntry {

    public String userId;
    public double longitude, latitude;
    public long time;

    public DbLocationEntry() {

    }

    public DbLocationEntry(String user, double longitude, double latitude, long time) {
        this.userId = user;
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
    }

    @Override
    public String toString() {
        return "User: " + this.userId + ", Loc: [" + this.longitude + ", " + this.latitude + "], at " + this.time;
    }
}
