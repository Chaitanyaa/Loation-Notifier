package com.example.saichaitanya.notify;

/**
 * Created by SAI CHAITANYA on 13-07-2015.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.HashMap;

/** Customizing AutoCompleteTextView to return Place Description
 * corresponding to the selected item
 */
public class AutoComplete extends AutoCompleteTextView {

    public AutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Returns the place description corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        MainActivity.placeId=hm.get("_id");
        return hm.get("description");
    }
}