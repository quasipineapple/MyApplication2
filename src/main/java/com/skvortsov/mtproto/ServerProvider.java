package com.skvortsov.mtproto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by skvortsov on 10/8/13.
 */
public class ServerProvider {
    private DatabaseHelper dbHelper = null;
    private static final String LOG_TAG = "ServerProvider";
    public static final String TABLE_NAME = "SERVER_INFO";
    public static final String KEY_ADDRESS = "ADDRESS";
    public static final String KEY_PORT = "PORT";

    /** Constructor */
    public ServerProvider(Context context) {
        Log.v(LOG_TAG, "context=" + context.toString());
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Get the database connection.
     */
    public SQLiteDatabase getConnection() {
        return dbHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        dbHelper.close();
    }

    /**
     * Get server information
     *
     */
    public Server getServerInfo() {
        Server server = null;
        String sql = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = dbHelper.getWritableDatabase().rawQuery(sql, null);
        Log.v(LOG_TAG, sql);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            server = new Server(cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)),
                    cursor.getInt(cursor.getColumnIndex(KEY_PORT)));


            Log.v(LOG_TAG,server.getAddress());
            Log.v(LOG_TAG,Integer.toString(server.getPort()));
        }

        cursor.close();


        return server;
    }

    /**
     * Update server information If server information has null value in
     * address or port, then Send a VERBOSE log message
     *
     * @param server
     *            The server information
     */
    public void updateServerInfo(Server server) {
        if (null != server.getAddress() && 0 != server.getPort()) {
            String sql = "update " + TABLE_NAME + " set " + KEY_ADDRESS + "='"
                    + server.getAddress() + "', " + KEY_PORT + "='"
                    + server.getPort() + "'";
            dbHelper.update(sql);
        } else {
            Log.v(LOG_TAG, "Cannot udpate server information, "
                    + "because of null value!");
        }
    }
}
