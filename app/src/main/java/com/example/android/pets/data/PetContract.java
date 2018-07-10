package com.example.android.pets.data;

import android.provider.BaseColumns;

public class PetContract {
    public static class PetEntry implements BaseColumns {
        public static final String TABLE_NAME = "pets";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "pet_name";
        public static final String COLUMN_PET_BREED = "pet_breed";
        public static final String COLUMN_PET_GENDER = "pet_gender";
        public static final String COLUMN_PET_WEIGHT = "pet_weight";
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}