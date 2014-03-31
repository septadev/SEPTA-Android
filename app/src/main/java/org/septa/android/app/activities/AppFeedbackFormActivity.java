/*
 * AppFeedbackFormActivity.java
 * Last modified on 02-06-2014 09:24-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.utilities.EmailLaunch;

public class AppFeedbackFormActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = AppFeedbackFormActivity.class.getName();

    private Uri selectedImageUri;

    private final int OBTAIN_IMAGE_REQUESTCODE = 1;
    private final int SEND_EMAIL_REQUESTCODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.app_feedback_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_appfeedbackform);
        getSupportActionBar().setTitle(titleText);

        final Spinner typeOfFeedbackSpinner = (Spinner)findViewById(R.id.type_of_feedback_spinner);
        final EditText detailsEditText = (EditText)findViewById(R.id.details_editText);

        final Button tapToSelectImageButton = (Button) findViewById(R.id.appfeedbackform_image_select_button);
        tapToSelectImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OBTAIN_IMAGE_REQUESTCODE);
            }
        });

        final Button submitAppFeedbackFormButton = (Button) findViewById(R.id.submit_button);
        submitAppFeedbackFormButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String subject = String.format(getString(R.string.appfeedbackform_subject_stringtemplate),
                        BuildConfig.VERSIONNAME,
                        typeOfFeedbackSpinner.getSelectedItem().toString());

                Intent intent = EmailLaunch.launchEmail(v.getContext(),
                        getString(R.string.appfeedbackform_sendto_emailaddress),
                        subject,
                        detailsEditText.getText().toString(),
                        selectedImageUri);

                try {

                    startActivityForResult(Intent.createChooser(intent, getString(R.string.mailto_chooser_text)),2);
                } catch (android.content.ActivityNotFoundException ex) {

                    Toast.makeText(AppFeedbackFormActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        detailsEditText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                Log.d(TAG, "onTextChanged with start, before, and count "+ start + " " + before + " " + count);
                if (start > 0) {
                    submitAppFeedbackFormButton.setEnabled(true);
                } else {
                    submitAppFeedbackFormButton.setEnabled(false);
                }
            }
        });
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
            case SEND_EMAIL_REQUESTCODE:
                Log.d(TAG, "result code of "+resultCode);
                final Spinner typeOfFeedbackSpinner = (Spinner)findViewById(R.id.type_of_feedback_spinner);
                typeOfFeedbackSpinner.setSelection(0);
                final EditText detailsEditText = (EditText)findViewById(R.id.details_editText);
                detailsEditText.setText("");
                this.selectedImageUri = null;

                break;
            default:
                Log.d(TAG, "a request code of "+requestCode+" was seen but not anticipated");
                break;
        }
    }
}
