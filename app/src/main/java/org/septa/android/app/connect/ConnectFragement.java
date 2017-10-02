package org.septa.android.app.connect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.septa.android.app.R;

/**
 * Created by jkampf on 9/29/17.
 */

public class ConnectFragement extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.connect_fragment_main, container, false);

        setHttpIntent(rootView, R.id.facebook_arrow, getResources().getString(R.string.facebook_url));
        setHttpIntent(rootView, R.id.twitter_arrow, getResources().getString(R.string.twitter_url));
        setHttpIntent(rootView, R.id.chat_arrow, getResources().getString(R.string.chat_url));
        setHttpIntent(rootView, R.id.comment_arrow, getResources().getString(R.string.comment_url));

        return rootView;
    }

    private void setHttpIntent(View rootView, int viewId, final String url) {
        View twitterLink = rootView.findViewById(viewId);
        twitterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    Uri webpage = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
