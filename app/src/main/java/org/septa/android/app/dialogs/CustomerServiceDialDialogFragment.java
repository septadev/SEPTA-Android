/*
 * CustomerServiceDialDialogFragment.java
 * Last modified on 02-02-2014 19:05-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.utilities.PhoneCallLaunch;

public class CustomerServiceDialDialogFragment extends DialogFragment {
    public static final String TAG = CustomerServiceDialDialogFragment.class.getName();

    TextView connectCustomerServiceDialogTitleTextView;
    Button connectCustomerServiceDialogMainTelephoneDialButton;
    Button connectCustomerServiceDialogTDDTTYTelephoneDialButton;
    Button connectCustomerServiceDialogCancelButton;

    public CustomerServiceDialDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogSlideAnim);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.connect_customerservice_dialog);

        // find the views by Id
        connectCustomerServiceDialogTitleTextView = (TextView) dialog.findViewById(R.id.connect_customerservice_dialog_title_textview);
        connectCustomerServiceDialogMainTelephoneDialButton = (Button) dialog.findViewById(R.id.connect_customerservice_dialog_main_telephone_dial_button);
        connectCustomerServiceDialogTDDTTYTelephoneDialButton = (Button) dialog.findViewById(R.id.connect_customerservice_dialog_tddtty_telephone_dial_button);
        connectCustomerServiceDialogCancelButton = (Button) dialog.findViewById(R.id.connect_customerservice_dialog_cancel_button);

        // set the text for the UI components
        connectCustomerServiceDialogTitleTextView.setText(R.string.connect_customerservice_dialog_title_text);
        connectCustomerServiceDialogMainTelephoneDialButton.setText(R.string.connect_customerservice_dialog_main_button_text);
        connectCustomerServiceDialogTDDTTYTelephoneDialButton.setText(R.string.connect_customerservice_dialog_tddtty_button_text);
        connectCustomerServiceDialogCancelButton.setText(R.string.connect_customerservice_dialog_cancel_button_text);

        // set the click listeners for each button

        connectCustomerServiceDialogMainTelephoneDialButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked the main telephone number dial button.");

                PhoneCallLaunch.launchPhoneCall(getActivity(), getString(R.string.main_telephone_number));

                // dismiss the dialog box
                dismiss();
            }
        });

        connectCustomerServiceDialogTDDTTYTelephoneDialButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked the tdd/tty telephone number dial button.");

                PhoneCallLaunch.launchPhoneCall(getActivity(), getString(R.string.tddtty_telephone_number));

                // dismiss the dialog box
                dismiss();
            }
        });

        connectCustomerServiceDialogCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked the cancel button");

                dismiss();
            }
        });

        dialog.show();

        return dialog;
    }
}

