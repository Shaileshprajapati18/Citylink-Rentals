package com.example.citylinkrentals.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.citylinkrentals.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class HelpAndSupportActivity extends AppCompatActivity {

    private ImageView backArrow;
    private LinearLayout whatsappSupport, phoneSupport, emailSupport;
    private LinearLayout faqPosting, faqPayment, faqAccount, faqSearch;
    private TextInputEditText etSubject, etDescription;
    private MaterialButton btnSubmitIssue;

    private static final String SUPPORT_PHONE = "+919876543210";
    private static final String SUPPORT_EMAIL = "support@property.com";
    private static final String WHATSAPP_NUMBER = "919876543210";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_support);

        initViews();
        setupListeners();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.main_background_color, getTheme()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                // decorView.setSystemUiVisibility(0);
            }
        }
    }

    private void initViews() {

        backArrow = findViewById(R.id.back_arrow);

        whatsappSupport = findViewById(R.id.whatsapp_support);
        phoneSupport = findViewById(R.id.phone_support);
        emailSupport = findViewById(R.id.email_support);

        // FAQ items
        faqPosting = findViewById(R.id.faq_posting);
        faqPayment = findViewById(R.id.faq_payment);
        faqAccount = findViewById(R.id.faq_account);
        faqSearch = findViewById(R.id.faq_search);

        // Report issue form
        etSubject = findViewById(R.id.et_subject);
        etDescription = findViewById(R.id.et_description);
        btnSubmitIssue = findViewById(R.id.btn_submit_issue);
    }

    private void setupListeners() {
        // Back button
        backArrow.setOnClickListener(v -> finish());

        // Quick help options
        whatsappSupport.setOnClickListener(v -> openWhatsApp());
        phoneSupport.setOnClickListener(v -> makePhoneCall());
        emailSupport.setOnClickListener(v -> sendEmail());

        // FAQ items
        faqPosting.setOnClickListener(v -> showFAQ("How to post a property?",
                "To post a property:\n\n" +
                        "1. Tap on 'Post Property' from the main menu\n" +
                        "2. Fill in all required property details\n" +
                        "3. Upload high-quality photos (1-10 images)\n" +
                        "4. Set your asking price\n" +
                        "5. Review and submit your listing\n\n" +
                        "Your property will be reviewed and published within 24 hours."));

        faqPayment.setOnClickListener(v -> showFAQ("Payment and Billing Issues",
                "Common payment issues:\n\n" +
                        "• Payment Failed: Check your internet connection and try again\n" +
                        "• Refund Queries: Refunds are processed within 3-5 business days\n" +
                        "• Billing Issues: Contact support with your transaction ID\n" +
                        "• Subscription: Manage subscriptions from your profile settings\n\n" +
                        "For immediate assistance, contact our support team."));

        faqAccount.setOnClickListener(v -> showFAQ("Account and Profile Settings",
                "Managing your account:\n\n" +
                        "• Profile Update: Go to Settings > Profile to edit details\n" +
                        "• Password Change: Use 'Forgot Password' option on login\n" +
                        "• Verification: Verify your phone and email for better security\n" +
                        "• Privacy Settings: Control who can see your contact details\n" +
                        "• Delete Account: Contact support to permanently delete your account"));

        faqSearch.setOnClickListener(v -> showFAQ("How to Search Properties?",
                "Finding the right property:\n\n" +
                        "1. Use the search bar on the home screen\n" +
                        "2. Apply filters: Location, Price, BHK, Property Type\n" +
                        "3. Use map view to see properties in your preferred area\n" +
                        "4. Save your favorite properties for later\n" +
                        "5. Contact owners directly through the app\n" +
                        "6. Use 'Recently Viewed' to revisit properties\n\n" +
                        "Tip: Enable location services for better search results!"));

        // Submit issue button
        btnSubmitIssue.setOnClickListener(v -> submitIssue());
    }

    private void openWhatsApp() {
        try {
            String message = "Hello! I need help with the Property App.";
            String url = "https://wa.me/" + WHATSAPP_NUMBER + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + SUPPORT_PHONE));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot make phone call", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - Property App");
            intent.putExtra(Intent.EXTRA_TEXT, "Hello Support Team,\n\nI need assistance with:\n\n");
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFAQ(String title, String content) {
        // Create and show FAQ dialog
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Contact Support", (dialog, which) -> {
                    dialog.dismiss();
                    openWhatsApp();
                })
                .create()
                .show();
    }

    private void submitIssue() {
        String subject = etSubject.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate inputs
        if (subject.isEmpty()) {
            etSubject.setError("Please enter a subject");
            etSubject.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Please describe your issue");
            etDescription.requestFocus();
            return;
        }

        // Show loading state
        btnSubmitIssue.setEnabled(false);
        btnSubmitIssue.setText("Submitting...");

        // Simulate API call (replace with actual API call)
        submitIssueToServer(subject, description);
    }

    private void submitIssueToServer(String subject, String description) {
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            // Reset button state
            btnSubmitIssue.setEnabled(true);
            btnSubmitIssue.setText("Submit Issue");

            // Clear form
            etSubject.getText().clear();
            etDescription.getText().clear();

            // Show success message
            showSuccessDialog();

            // In real implementation, make API call here:
            /*
            ApiService.submitSupportTicket(subject, description, new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, Response<Response> response) {
                    if (response.isSuccessful()) {
                        showSuccessDialog();
                        clearForm();
                    } else {
                        showErrorDialog("Failed to submit issue. Please try again.");
                    }
                    resetButtonState();
                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    showErrorDialog("Network error. Please check your connection.");
                    resetButtonState();
                }
            });
            */

        }, 2000); // 2 second delay to simulate network request
    }

    private void showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Issue Submitted Successfully!")
                .setMessage("Thank you for reaching out. We've received your issue and our support team will get back to you within 24 hours.\n\nTicket ID: #" + generateTicketId())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_verified)
                .create()
                .show();
    }

    private void showErrorDialog(String message) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Try WhatsApp", (dialog, which) -> {
                    dialog.dismiss();
                    openWhatsApp();
                })
                .create()
                .show();
    }

    private String generateTicketId() {
        // Generate a simple ticket ID (in real app, this would come from server)
        return "SP" + System.currentTimeMillis() % 100000;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}