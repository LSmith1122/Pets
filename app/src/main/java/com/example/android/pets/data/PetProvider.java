package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {
    private final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDBHelper DBHelper;
    public static final int PETS = 100;
    public static final int PETS_ID = 101;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);       // sUriMatcher - the "s" means that its static

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS_ID, PETS_ID);
    }

    @Override
    public boolean onCreate() {
        DBHelper = new PetDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        try {
            DBHelper = new PetDBHelper(getContext());
            SQLiteDatabase db = DBHelper.getReadableDatabase();
            switch (sUriMatcher.match(uri)) {
                case PETS:
                    cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case PETS_ID:
                    selection = PetEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid URI");
            }
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Error queriing database.", e);
        }
        if (cursor == null) {
            return null;
        } else {
            return cursor;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        int badID = -1;
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        String breed = values.getAsString(PetEntry.COLUMN_PET_BREED);
        int gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        int weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        contentValues.put(PetEntry.COLUMN_PET_NAME, name);
        contentValues.put(PetEntry.COLUMN_PET_BREED, breed);
        contentValues.put(PetEntry.COLUMN_PET_GENDER, gender);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        long newRowID = db.insert(PetEntry.TABLE_NAME, null, contentValues);
        if (newRowID != badID) {
            Toast.makeText(getContext(), "Pet saved!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error saving pet...", Toast.LENGTH_SHORT).show();
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowID);
    }

    private String checkGender(int value) {
        switch (value) {
            case PetContract.PetEntry.GENDER_MALE:
                return PetContract.PetEntry.GENDER_STRING_MALE;
            case PetContract.PetEntry.GENDER_FEMALE:
                return PetContract.PetEntry.GENDER_STRING_FEMALE;
            default:
                return PetContract.PetEntry.GENDER_STRING_UNKOWN;
        }
    }

    private int checkGender(String value) {
        switch (value) {
            case PetContract.PetEntry.GENDER_STRING_MALE:
                return PetContract.PetEntry.GENDER_MALE;
            case PetContract.PetEntry.GENDER_STRING_FEMALE:
                return PetContract.PetEntry.GENDER_FEMALE;
            default:
                return PetContract.PetEntry.GENDER_UNKNOWN;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
