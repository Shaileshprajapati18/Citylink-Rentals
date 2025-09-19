package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.citylinkrentals.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ImageView backArrow, shareButton;
    private TextView tvLastUpdated;
    private MaterialButton btnContactSupport;

    private LinearLayout tocInformationCollection, tocInformationUse, tocInformationSharing;
    private LinearLayout tocDataSecurity, tocUserRights, tocContactInfo;

    private ImageView arrowInformationCollection, arrowInformationUse, arrowInformationSharing;
    private ImageView arrowDataSecurity, arrowUserRights, arrowContactInfo;

    private LinearLayout contentInformationCollection, contentInformationUse, contentInformationSharing;
    private LinearLayout contentDataSecurity, contentUserRights, contentContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        initViews();
        setupListeners();
        updateLastModifiedDate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.main_background_color, getTheme()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void initViews() {
        backArrow = findViewById(R.id.back_arrow);
        shareButton = findViewById(R.id.share_button);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        btnContactSupport = findViewById(R.id.btn_contact_support);

        tocInformationCollection = findViewById(R.id.toc_information_collection);
        tocInformationUse = findViewById(R.id.toc_information_use);
        tocInformationSharing = findViewById(R.id.toc_information_sharing);
        tocDataSecurity = findViewById(R.id.toc_data_security);
        tocUserRights = findViewById(R.id.toc_user_rights);
        tocContactInfo = findViewById(R.id.toc_contact_info);

        arrowInformationCollection = findViewById(R.id.arrow_information_collection);
        arrowInformationUse = findViewById(R.id.arrow_information_use);
        arrowInformationSharing = findViewById(R.id.arrow_information_sharing);
        arrowDataSecurity = findViewById(R.id.arrow_data_security);
        arrowUserRights = findViewById(R.id.arrow_user_rights);
        arrowContactInfo = findViewById(R.id.arrow_contact_info);

        contentInformationCollection = findViewById(R.id.content_information_collection);
        contentInformationUse = findViewById(R.id.content_information_use);
        contentInformationSharing = findViewById(R.id.content_information_sharing);
        contentDataSecurity = findViewById(R.id.content_data_security);
        contentUserRights = findViewById(R.id.content_user_rights);
        contentContactInfo = findViewById(R.id.content_contact_info);
    }

    private void setupListeners() {

        backArrow.setOnClickListener(v -> finish());

        shareButton.setOnClickListener(v -> sharePrivacyPolicy());

        tocInformationCollection.setOnClickListener(v -> toggleSection(
                contentInformationCollection, arrowInformationCollection));
        tocInformationUse.setOnClickListener(v -> toggleSection(
                contentInformationUse, arrowInformationUse));
        tocInformationSharing.setOnClickListener(v -> toggleSection(
                contentInformationSharing, arrowInformationSharing));
        tocDataSecurity.setOnClickListener(v -> toggleSection(
                contentDataSecurity, arrowDataSecurity));
        tocUserRights.setOnClickListener(v -> toggleSection(
                contentUserRights, arrowUserRights));
        tocContactInfo.setOnClickListener(v -> toggleSection(
                contentContactInfo, arrowContactInfo));

        btnContactSupport.setOnClickListener(v -> openHelpSupport());
    }

    private void toggleSection(LinearLayout content, ImageView arrow) {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            arrow.animate().rotation(0).setDuration(200).start();
        } else {
            content.setVisibility(View.VISIBLE);
            arrow.animate().rotation(180).setDuration(200).start();
        }
    }

    private void updateLastModifiedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvLastUpdated.setText("Last Updated: " + currentDate);
    }

    private void sharePrivacyPolicy() {
        String shareText = "Check out the Privacy Policy of Property App: \n\n" +
                "We are committed to protecting your privacy and ensuring the security of your personal information.\n\n" +
                "Download the app to read our complete Privacy Policy.";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Privacy Policy - Property App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Privacy Policy"));
    }

    private void openHelpSupport() {
        Intent intent = new Intent(this, HelpAndSupportActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}