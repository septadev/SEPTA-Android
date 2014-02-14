/*
 * CommentsFormActionBarActivity.java
 * Last modified on 02-13-2014 18:37-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.utilities.EmailLaunch;

public class CommentsFormActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = CommentsFormActionBarActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate... what's up?");
        super.onCreate(savedInstanceState);

        String actionBarTitleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));
        String iconImageNameSuffix = getIntent().getStringExtra(getString(R.string.actionbar_iconimage_imagenamesuffix_key));

        String resourceName = getString(R.string.actionbar_iconimage_imagename_base).concat(iconImageNameSuffix);

        int id = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(actionBarTitleText);
        getSupportActionBar().setIcon(id);

        setContentView(R.layout.comments_form);

        final Button submitCommentsFormButton = (Button) findViewById(R.id.commentsForm_submit_button);
        final EditText nameEditText = (EditText) findViewById(R.id.commentsForm_name_editText);
        final EditText phoneEditText = (EditText) findViewById(R.id.commentsForm_phone_editText);
        final EditText emailEditText = (EditText) findViewById(R.id.commentsForm_email_editText);

        /** disable the comments form submit button to start with */
        submitCommentsFormButton.setEnabled(false);

        submitCommentsFormButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.d(TAG, "detected the click on the submit button");
            }
        });

        TextWatcher editTextFieldWatcher = new TextWatcher(){
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged...");
                if (nameEditText.getText().length() > 0 ||
                    phoneEditText.getText().length() > 0 ||
                    emailEditText.getText().length() > 0) {

                        submitCommentsFormButton.setEnabled(true);
                } else {

                    submitCommentsFormButton.setEnabled(false);
                }
            }
        };

        nameEditText.addTextChangedListener(editTextFieldWatcher);
        phoneEditText.addTextChangedListener(editTextFieldWatcher);
        emailEditText.addTextChangedListener(editTextFieldWatcher);
    }
}
