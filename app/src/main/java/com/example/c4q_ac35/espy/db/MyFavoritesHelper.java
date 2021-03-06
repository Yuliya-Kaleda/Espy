package com.example.c4q_ac35.espy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.c4q_ac35.espy.foursquare.Contact;
import com.example.c4q_ac35.espy.foursquare.Location;
import com.example.c4q_ac35.espy.foursquare.Venue;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by c4q-ac35 on 9/10/15.
 */
public class MyFavoritesHelper extends OrmLiteSqliteOpenHelper {

    private static final String MYDB = "mydb.db";
    private static final int VERSION = 1;
    public static final String MY_NAME = MyFavoritesHelper.class.getName();

    private Dao<Venue, Integer> venueDao = null;
    private Dao<Contact,Integer> contactDao = null;
    private Dao<Location,Integer> locationDao = null;

    public MyFavoritesHelper(Context context) {
        super(context, MYDB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(MY_NAME, "onCreate");
            TableUtils.createTable(connectionSource, Venue.class);
            TableUtils.createTable(connectionSource, Location.class);

            //...

//            Dao<Venue, Integer> dao = getVenueDao();
//            Venue v = new Venue();
//            v.setId("123");
//            v.setName("Jose");
//            v.setName(someTextView.getText());
//            dao.create(v);

            Log.i(MY_NAME, "created new entries in onCreate");
        } catch (SQLException e) {
            Log.e(MY_NAME, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if(oldVersion == newVersion) return;

        try {
            Log.i(MY_NAME, "onUpgrade");

            //convert old table schema to new table schema
            TableUtils.dropTable(connectionSource, Venue.class, true);
            onCreate(db, connectionSource);

        } catch (SQLException e) {
            Log.e(MY_NAME, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Venue, Integer> getVenueDao() throws SQLException {
        if (venueDao == null) {
            venueDao = getDao(Venue.class);
        }
        return venueDao;
    }
    public Dao<Contact, Integer> getContactDao() throws SQLException {
        if (contactDao == null) {
            contactDao = getDao(Contact.class);
        }
        return contactDao;
    }
    public Dao<Location, Integer> getLocationDao() throws SQLException {
        if (locationDao == null) {
            locationDao = getDao(Location.class);
        }
        return locationDao;
    }

    @Override
    public void close() {
        super.close();
        venueDao = null;
    }
}
