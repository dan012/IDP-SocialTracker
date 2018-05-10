package cs.idp.dvornicu.socialtracker.utils;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DbChatEntry {

    public String message;
    public long time;

    public DbChatEntry() {

    }

    public DbChatEntry(String message, long time) {
        this.message = message;
        this.time = time;
    }

    @Override
    public String toString() {
        return this.message + " [" + this.time + "]";
    }
}
