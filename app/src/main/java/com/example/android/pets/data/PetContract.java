package com.example.android.pets.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class PetContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PETS = "pets";
    public static final String PATH_PETS_ID = PATH_PETS + "/#";

    public static class PetEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);
        public static final String TABLE_NAME = "pets";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "pet_name";
        public static final String COLUMN_PET_BREED = "pet_breed";
        public static final String COLUMN_PET_GENDER = "pet_gender";
        public static final String COLUMN_PET_WEIGHT = "pet_weight";
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final String GENDER_STRING_UNKOWN = "Unknown";
        public static final String GENDER_STRING_MALE = "Male";
        public static final String GENDER_STRING_FEMALE = "Female";
    }
}