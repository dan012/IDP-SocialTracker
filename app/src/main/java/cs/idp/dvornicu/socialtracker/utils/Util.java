package cs.idp.dvornicu.socialtracker.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cs.idp.dvornicu.socialtracker.MainActivity;

public final class Util {

    Util() { }

    public static String timestampToDate(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        Date netDate = (new Date(timestamp));
        return df.format(netDate);
    }

    public static StorageManager getStorageManager() {
        return MainActivity.getStorageManager();
    }

    public static FirebaseDatabase getDb() {
        return FirebaseDatabase.getInstance();
    }
}
