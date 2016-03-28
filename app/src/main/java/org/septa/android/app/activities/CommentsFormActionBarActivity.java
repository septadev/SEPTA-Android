/*
 * CommentsFormActionBarActivity.java
 * Last modified on 02-13-2014 18:37-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.utilities.EmailLaunch;

import java.util.ArrayList;
import java.util.List;

public class CommentsFormActionBarActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = CommentsFormActionBarActivity.class.getName();

    private static final int OBTAIN_IMAGE_REQUESTCODE = 1;

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText ;

    private EditText locationEditText;
    private EditText destinationEditText ;
    private EditText routeEditText ;
    private EditText vehicleEditText;
    private EditText blockEditText;
    private EditText directionEditText;

    private Spinner commentTypeSpinner;

    private EditText descriptionEditText;
    private EditText detailsEditText;

    private Uri selectedImageUri;

    private List<NameValuePair> collectFormFieldValues() {
        List<NameValuePair> formValuesList = new ArrayList<NameValuePair>();
        formValuesList.add(new BasicNameValuePair("App version", BuildConfig.VERSIONNAME));
        formValuesList.add(new BasicNameValuePair("recipient", getString(R.string.commentsForm_recipient)));
        formValuesList.add(new BasicNameValuePair("wl_tp", getString(R.string.commentsForm_wl_tp)));
        formValuesList.add(new BasicNameValuePair("subject", getString(R.string.commentsForm_subject)));
        formValuesList.add(new BasicNameValuePair("required", getString(R.string.commentsForm_required)));

        formValuesList.add(new BasicNameValuePair("Name", nameEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("phone", phoneEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("email", emailEditText.getText().toString()));
        formValuesList.add(new BasicNameValuePair("Incident Date", ""));  // TODO: confirm this is correct, it is empty in the iOS code
        formValuesList.add(new BasicNameValuePair("Boarding LocationModel", locationEditText.getText().toString()));
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

                sendEmail(collectFormFieldValues());
                resetFormFields();
            }
        });

        nameEditText = (EditText) findViewById(R.id.commentsForm_name_editText);
        phoneEditText = (EditText) findViewById(R.id.commentsForm_phone_editText);
        emailEditText = (EditText) findViewById(R.id.commentsForm_email_editText);

        locationEditText = (EditText) findViewById(R.id.commentsForm_location_editText);
        destinationEditText = (EditText) findViewById(R.id.commentsForm_destination_editText);
        routeEditText = (EditText) findViewById(R.id.commentsForm_route_editText);
        vehicleEditText = (EditText) findViewById(R.id.commentsForm_vehicle_editText);
        blockEditText = (EditText) findViewById(R.id.commentsForm_block_editText);
        directionEditText = (EditText) findViewById(R.id.commentsForm_direction_editText);

        commentTypeSpinner = (Spinner) findViewById(R.id.commentsForm_commentType_spinner);

        descriptionEditText = (EditText) findViewById(R.id.commentsForm_description_editText);
        detailsEditText = (EditText) findViewById(R.id.commentsForm_details_editText);

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

        Button attachImageButton = (Button) findViewById(R.id.commentsForm_attach_image_button);
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OBTAIN_IMAGE_REQUESTCODE);
            }
        });
    }

    private void resetFormFields() {
        nameEditText.setText("");
        phoneEditText.setText("");
        emailEditText.setText("");

        locationEditText.setText("");
        destinationEditText.setText("");
        routeEditText.setText("");
        vehicleEditText.setText("");
        blockEditText.setText("");
        directionEditText.setText("");

        commentTypeSpinner.setSelection(0);

        descriptionEditText.setText("");
        detailsEditText.setText("");

        nameEditText.requestFocus();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case OBTAIN_IMAGE_REQUESTCODE:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "received a result ok for the activity result to obtain an image");
                    this.selectedImageUri = imageReturnedIntent.getData();
                } else {
                    Log.d(TAG, "receive a result of not okay for the activity result to obtain an image");
                }
                break;
            default:
                Log.d(TAG, "a request code of "+requestCode+" was seen but not anticipated");
                break;
        }
    }

    private void sendEmail(List<NameValuePair> nameValuePairs) {
        StringBuilder body = new StringBuilder();
        for( NameValuePair pair: nameValuePairs) {
            body.append(pair.getName());
            body.append(" : ");
            body.append(pair.getValue());
            body.append("\n");
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.commentsForm_recipient)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.commentsForm_subject_stringtemplate, BuildConfig.VERSIONNAME));
        emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
        if (this.selectedImageUri != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, this.selectedImageUri);
        }

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "Unable to send email", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "There are no email clients installed.");
        }
    }
}
