package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.R;
import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {
    private final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDBHelper DBHelper;
    private final int BAD_ID = -1;
    private final int zero = 0;
    public static final int PETS = 100;
    public static final int PETS_ID = 101;
    public static final int PETS_NAME = 102;
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
                    throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_argument_exception_invalid_uri));
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
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_argument_exception_insertion_not_supported) + uri);
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
        if (name == null || TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_exception_invalid_name));
        }
        if (!PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_exception_invalid_gender));
        }
        if (weight <= 0) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_exception_invalid_weight));
        }
        contentValues.put(PetEntry.COLUMN_PET_NAME, name);
        contentValues.put(PetEntry.COLUMN_PET_BREED, breed);
        contentValues.put(PetEntry.COLUMN_PET_GENDER, gender);
        contentValues.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        long newRowID = db.insert(PetEntry.TABLE_NAME, null, contentValues);
        if (newRowID != badID) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.update_pet_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.update_error_saving_pet), Toast.LENGTH_SHORT).show();
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
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                int deletedRows = deletePet(uri, selection, selectionArgs);
                return deletedRows;
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return deletePet(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_argument_exception_deletion_not_supported) + " " + uri);
        }
    }

    private int deletePet(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        int deletedRows = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.illegal_argument_exception_update_not_supported) + " " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int newRowID = BAD_ID;
        ContentValues newValues = new ContentValues();
        if (values.size() == zero) {
            Log.i("TEST", "Values equal 0");
            return zero;
        } else {
            SQLiteDatabase db = DBHelper.getWritableDatabase();
            if (checkForValidKey(values, PetEntry.COLUMN_PET_NAME)) {
                putValueToNewContentValues(PetEntry.COLUMN_PET_NAME, values, newValues);
            }
            if (checkForValidKey(values, PetEntry.COLUMN_PET_BREED)) {
                putValueToNewContentValues(PetEntry.COLUMN_PET_BREED, values, newValues);
            }
            if (checkForValidKey(values, PetEntry.COLUMN_PET_GENDER)) {
                putValueToNewContentValues(PetEntry.COLUMN_PET_GENDER, values, newValues);
            }
            if (checkForValidKey(values, PetEntry.COLUMN_PET_WEIGHT)) {
                putValueToNewContentValues(PetEntry.COLUMN_PET_WEIGHT, values, newValues);
            }
            if (checkContentValues(values, PetEntry.PET_ATTRIBUTE_LIST)) {      // attribute input provided
                newRowID = db.update(PetEntry.TABLE_NAME, newValues, selection, selectionArgs);
            } else {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.error_input_provide_attributes));
            }
        }
        return newRowID;
    }

    private boolean checkForValidKey(ContentValues values, String key) {
        if (values.containsKey(key) && !TextUtils.isEmpty(String.valueOf(values.get(key)))) {
            return true;
        }
        return false;
    }

    private void putValueToNewContentValues(String key, ContentValues oldValues, ContentValues newValues) {
        String value = null;
        if (oldValues.get(key) instanceof  String) {                    // is a String
            value = oldValues.getAsString(key);
        } else if (oldValues.get(key) instanceof Integer) {             // is an Integer
            value = String.valueOf(oldValues.getAsInteger(key));
        } else if (oldValues.get(key) instanceof Double) {              // is a Double... for some reason
            value = String.valueOf(oldValues.getAsDouble(key));
        }
        newValues.put(key, value);
    }

    private boolean checkContentValues(ContentValues values, String[] list) {
        int listSize = list.length;
        int keyAmount = 0;
        for (int i = zero; i < listSize; i++) {
            if (values.containsKey(list[i])) {
                keyAmount++;
            }
        }
        if (keyAmount <= zero) {
            return false;
        }
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_DIR_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}