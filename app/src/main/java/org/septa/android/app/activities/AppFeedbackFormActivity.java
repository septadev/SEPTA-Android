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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.septa.android.app.BuildConfig;
import org.septa.android.app.R;
import org.septa.android.app.utilities.EmailLaunch;

public class AppFeedbackFormActivity extends BaseAnalyticsActionBarActivity {
    public static final String TAG = AppFeedbackFormActivity.class.getName();

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String titleText = getIntent().getStringExtra(getString(R.string.actionbar_titletext_key));

        setContentView(R.layout.app_feedback_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_actionbar_appfeedbackform);
        getSupportActionBar().setTitle(titleText);

        final Button tapToSelectImageButton = (Button) findViewById(R.id.appfeedbackform_image_select_button);
        tapToSelectImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        final Spinner typeOfFeedbackSpinner = (Spinner)findViewById(R.id.type_of_feedback_spinner);
        final EditText detailsEditText = (EditText)findViewById(R.id.details_editText);
        final Button submitAppFeedbackFormButton = (Button) findViewById(R.id.submit_button);
        submitAppFeedbackFormButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String subject = String.format(getString(R.string.appfeedbackform_subject_stringtemplate),
                        BuildConfig.VERSIONNAME,
                        typeOfFeedbackSpinner.getSelectedItem().toString());
                EmailLaunch.launchEmail(v.getContext(),
                        getString(R.string.appfeedbackform_sendto_emailaddress),
                        subject,
                        detailsEditText.getText().toString(),
                        selectedImageUri);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){

                    this.selectedImageUri = imageReturnedIntent.getData();
                }
                break;
        }
    }
}
