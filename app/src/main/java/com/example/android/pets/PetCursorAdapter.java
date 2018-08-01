package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PetCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public PetCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return convertView;
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            try {
                String divider = " - ";
                TextView tvName = (TextView) view.findViewById(R.id.name);
                TextView tvSummary = (TextView) view.findViewById(R.id.summary);
                int idColumnIndex = cursor.getColumnIndexOrThrow(PetEntry._ID);
                int nameColumnIndex = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME);
                int breedColumnIndex = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED);
                int genderColumnIndex = cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER);
                String currentID = String.valueOf(cursor.getInt(idColumnIndex));
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                String currentGender = "";
                int genderInteger = cursor.getInt(genderColumnIndex);
                switch (genderInteger) {
                    case PetEntry.GENDER_MALE:
                        currentGender = context.getString(R.string.gender_male);
                        break;
                    case PetEntry.GENDER_FEMALE:
                        currentGender = context.getString(R.string.gender_female);
                        break;
                    default:
                        currentGender = context.getString(R.string.gender_unknown);
                        break;
                }
                StringBuilder builder = new StringBuilder();
                builder.append(currentBreed).append(divider).append(currentGender);
                tvName.setText(currentName);
                tvSummary.setText(builder.toString());
                Log.i("INFO", "Current Pet: " + currentID + " " + currentName + " " + currentBreed + " " + currentGender);
            } catch (IllegalArgumentException e) {
                Log.e("Error", "Error binding view with Cursor data", e);
            }
        }
    }
}