package com.example.android.pets.data;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetDBHelper extends SQLiteOpenHelper {
    private static Context mContext;
    public static final String DATABASE_NAME = "pets.db";
    public static final int DATABASE_VERSION = 1;
    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PETS_TABLE =
                "CREATE TABLE " + PetEntry.TABLE_NAME + "("
                        + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL,"
                        + PetEntry.COLUMN_PET_BREED + " TEXT,"
                        + PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL DEFAULT 0,"
                        + PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}