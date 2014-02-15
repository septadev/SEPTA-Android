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
import android.widget.Spinner;

import org.apache.http.message.BasicNameValuePair;
import org.septa.android.app.R;
import org.septa.android.app.strategies.CommentsFormSubmissionStrategy;

import java.util.ArrayList;
import java.util.List;

public class CommentsFormActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = CommentsFormActionBarActivity.class.getName();

    private List collectFormFieldValues() {
        final EditText nameEditText = (EditText) findViewById(R.id.commentsForm_name_editText);
        final EditText phoneEditText = (EditText) findViewById(R.id.commentsForm_phone_editText);
        final EditText emailEditText = (EditText) findViewById(R.id.commentsForm_email_editText);

        final EditText locationEditText = (EditText) findViewById(R.id.commentsForm_location_editText);
        final EditText destinationEditText = (EditText) findViewById(R.id.commentsForm_destination_editText);
        final EditText routeEditText = (EditText) findViewById(R.id.commentsForm_route_editText);
        final EditText vehicleEditText = (EditText) findViewById(R.id.commentsForm_vehicle_editText);
        final EditText blockEditText = (EditText) findViewById(R.id.commentsForm_block_editText);
        final EditText directionEditText = (EditText) findViewById(R.id.commentsForm_direction_editText);

        final Spinner commentTypeSpinner = (Spinner) findViewById(R.id.commentsForm_commentType_spinner);

        final EditText descriptionEditText = (EditText) findViewById(R.id.commentsForm_description_editText);
        final EditText detailsEditText = (EditText) findViewById(R.id.commentsForm_details_editText);

        List formValuesList = new ArrayList();
        formValuesList.add(new BasicNameValuePair("recipient", getString(R.string.commentsForm_recipient)));
        formValuesList.add(new BasicNameValuePair("wl_tp", getString(R.string.commentsForm_wl_tp)));
        formValuesList.add(new BasicNameValuePair("subject", getString(R.string.commentsForm_subject)));
        formValuesList.add(new BasicNameValuePair("required", getString(R.string.commentsForm_required)));

        formValuesList.add(new BasicNameValuePair("Name", nameEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("phone", phoneEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("email", emailEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Incident Date", ""));  // TODO: confirm this is correct, it is empty in the iOS code
        formValuesList.add(new BasicNameValuePair("Boarding Location", locationEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("route", routeEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("vehicle", vehicleEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("block", blockEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Time of Incident", " "));  // TODO: confirm this is correct, it is empty in the iOS code
        formValuesList.add(new BasicNameValuePair("Final Destination", destinationEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Direction of Travel", directionEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Comment", commentTypeSpinner.getSelectedItem().toString()));
        formValuesList.add(new BasicNameValuePair("Employee", descriptionEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Comments", detailsEditText.getText().toString()));

        return formValuesList;
    }

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
        /** disable the comments form submit button to start with */
        submitCommentsFormButton.setEnabled(false);

        submitCommentsFormButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.d(TAG, "detected the click on the submit button");
                Log.d(TAG, "about to execute the network call with the form field values...");
                new CommentsFormSubmissionStrategy(getApplicationContext()).execute(collectFormFieldValues());
                Log.d(TAG, "executed the network call with the form field values");
            }
        });

        final EditText nameEditText = (EditText) findViewById(R.id.commentsForm_name_editText);
        final EditText phoneEditText = (EditText) findViewById(R.id.commentsForm_phone_editText);
        final EditText emailEditText = (EditText) findViewById(R.id.commentsForm_email_editText);

        TextWatcher editTextFieldWatcher = new TextWatcher(){
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged...");
                if (nameEditText.getText().length() > 0 &&
                    phoneEditText.getText().length() > 0 &&
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
