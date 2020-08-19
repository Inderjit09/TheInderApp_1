package com.inderproduction.theinderapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inderproduction.theinderapp.Modals.UserProfile;
import com.inderproduction.theinderapp.Utilities.CustomUtils;
import com.inderproduction.theinderapp.Utilities.DisplayUtils;
import com.inderproduction.theinderapp.Utilities.Validations;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private EditText profileName, profilePhone, profileAddress;
    Button profileSave, profileCustomButton;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private RelativeLayout ll_paypal;
    private ProgressBar profileProgressBar;
    private ImageView profileImage;
    private DocumentReference profileReference;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference profilePicReference = storage.getReference("profiles");

    private CollectionReference ordersColReference = firebaseFirestore.collection("orders");

    private boolean isSenderCart = false;
    private UserProfile profile;

    private String eMail;


    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

    private static final String CONFIG_CLIENT_ID = "AQyoujfIXjlOokovrur6SoPaj5jm87eN4SN-8Dr_nl2oHeEspXsLCEmzQ8Kp3NodyKpgD_Y0QSBWy5DY";
    private static final String CONFIG_SECRET_ID = "EILkK5CSEFrrbIR15jLEVOdB4NQDVdwWq66IGVHnuoZANJ91uThjSqVn954h-vuRpDv0ODhfznsd07iN";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            .sandboxUserPassword(CONFIG_SECRET_ID)

            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("The Inder Store")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

    PayPalPayment thingToBuy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        if (auth.getCurrentUser() == null) {
            DisplayUtils.showToast(this, "Authentication Failed", Toast.LENGTH_SHORT);
            finish();
        } else {
            eMail = auth.getCurrentUser().getEmail().toLowerCase();
            profileReference = firebaseFirestore.collection("profiles").document(auth.getUid());
        }

        ll_paypal = findViewById(R.id.ll_paypal);
        profileName = findViewById(R.id.profile_fname);
        profilePhone = findViewById(R.id.profile_phone);
        profileAddress = findViewById(R.id.profile_address);
        profileSave = findViewById(R.id.profile_save_btn);
        profileImage = findViewById(R.id.profile_image);
        profileCustomButton = findViewById(R.id.profile_custom_button);
        profileProgressBar = findViewById(R.id.profile_progress_bar);

        profilePicReference.child(auth.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Picasso.get().load(task.getResult()).into(profileImage);
                }
            }
        });

   /*     profileReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                if (task.isSuccessful()) {
                    if (task.getResult().exists() && task.getResult() != null) {
                        profile = task.getResult().toObject(UserProfile.class);
                        profileName.setText(profile.getUserFullName());
                        profileAddress.setText(profile.getUserAddress());
                        profilePhone.setText(profile.getUserPhone() + "");
                        Log.e("ENCRYPTED", String.valueOf(profile.getEncrypted()));

                        if (!profile.getEncrypted()) {
                            if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(UserProfileActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        101);

                            } else {

                            }

                        }
                        profileProgressBar.setVisibility(View.INVISIBLE);
                        DisplayUtils.enableFields(profileName, profilePhone, profileAddress, profileSave);
                    } else {
                        DisplayUtils.showToast(UserProfileActivity.this, "Could Not Load Profile", Toast.LENGTH_SHORT);
                        finish();
                    }
                } else {
                    profileProgressBar.setVisibility(View.INVISIBLE);

                }
            }
        });*/


        if (getIntent().getStringExtra("sender") != null) {
            isSenderCart = true;
            //    DisplayUtils.disableFields(profileName, profilePhone, profileAddress, profileCustomButton, profileSave);
            profileImage.setVisibility(View.INVISIBLE);
            //  profileCustomButton.setText("Proceed to Checkout");
            profileSave.setText("Confirm Profile");
        } else {
            profileSave.setText("Save");
            profileCustomButton.setText("Log Out");
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(UserProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            101);

                } else {

                    takeImageIntent();
                }

            }
        });

        profileSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Validations.isGenericallyValid(profileName) && Validations.isNumberValid(profilePhone)
                        && Validations.isGenericallyValid(profileAddress)) {

                    if (isSenderCart) {
                        String id = "ORDER_" + new Date().getTime();
                        OneApplication.finalOrder.setOrderID(id);
                        OneApplication.finalOrder.setOrderTime(Timestamp.now());
                        OneApplication.finalOrder.setCustomerName(CustomUtils.getTextFromField(profileName));
                        OneApplication.finalOrder.setCustomerEmail(eMail);
                        OneApplication.finalOrder.setCustomerNumber(CustomUtils.getTextFromField(profilePhone));
                        OneApplication.finalOrder.setCustomerAddress(CustomUtils.getTextFromField(profileAddress));
                        DisplayUtils.enableFields(profileCustomButton);


                        //paypal code

                        thingToBuy = new PayPalPayment(new BigDecimal(OneApplication.finalOrder.getOrderGrandTotal()), "USD", "product", PayPalPayment.PAYMENT_INTENT_SALE);
                        Intent intent = new Intent(UserProfileActivity.this, PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                        startActivityForResult(intent, REQUEST_CODE_PAYMENT);


                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("userFullName", CustomUtils.getTextFromField(profileName));
                        long phone = Long.parseLong(CustomUtils.getTextFromField(profilePhone));
                        data.put("userPhone", phone);
                        data.put("userAddress", profileAddress.getText().toString().trim());
                        if (profile == null) {
                            data.put("encrypted", false);
                        }
                        profileReference.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DisplayUtils.showToast(UserProfileActivity.this, "Profile Saved", Toast.LENGTH_SHORT);
                                } else {
                                    DisplayUtils.showToast(UserProfileActivity.this, "Error Saving Profile", Toast.LENGTH_SHORT);
                                }

                                DisplayUtils.enableFields(profileName, profilePhone, profileAddress, profileSave, profileCustomButton);
                            }
                        });
                    }
                }
            }
        });

        profileCustomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSenderCart) {
                    saveOrderInCollection();
                } else {
                    auth.signOut();
                    finish();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeImageIntent();
            new Thread(new Runnable() {
                @Override
                public void run() {
                }
            }).start();
        } else {

        }
    }


    private void takeImageIntent() {
        Intent takeImage = new Intent(Intent.ACTION_GET_CONTENT);
        takeImage.setType("image/*");
        Intent intent = Intent.createChooser(takeImage, "Please select image");
        startActivityForResult(intent, 110);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 110 && resultCode == RESULT_OK && data != null) {
            {
                profilePicReference.child(auth.getUid()).putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Picasso.get().load(data.getData()).into(profileImage);
                            DisplayUtils.showToast(UserProfileActivity.this, "Profile Pic Updated!", Toast.LENGTH_SHORT);
                        } else {
                            DisplayUtils.showToast(UserProfileActivity.this, "Error Storing Image", Toast.LENGTH_SHORT);
                        }
                    }
                });

            }
        } else if (requestCode == REQUEST_CODE_PAYMENT && resultCode == Activity.RESULT_OK) {


            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    ProofOfPayment proofOfPayment = confirm.getProofOfPayment();
                    JSONObject jsonObject = confirm.getPayment().toJSONObject();

                    String paymentState = proofOfPayment.getState();

                    if (paymentState.equalsIgnoreCase("approved")) {
                    /*amount = jsonObject.optInt("amount");
                    currencyCode = jsonObject.optString("currency_code");
                    short_description = jsonObject.optString("short_description");
                    intent = jsonObject.optString("intent");// Sale

                    id = proofOfPayment.getPaymentId();
                    dateTime = proofOfPayment.getCreateTime();
                    authIDPayment = proofOfPayment.getAuthorizationId();*/

                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject().toString(4));

                        String status = "";

                        if (paymentState.equalsIgnoreCase("approved")) {
                            status = "Success";
                        } else {
                            status = "Failed";
                        }
                        Log.e("status", status);
                        saveOrderInCollection();
                        //  savePaymentDetails(amount, FeverApplication.getInstance().useInt("type"), status, status, 1, "Paypal", id);

                            /*if (CheckInternetConnection.checkConnection(BottomNavigationActivity.this)) {
                                progressBar.show();
                                saveTransaction();
                                saveTransaction();
                            } else {
                                DwitterApplication.getInstance().showDialog(BottomNavigationActivity.this, "Internet Error", "No Internet Found!!");
                            }*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        } else if (resultCode == Activity.RESULT_CANCELED) {
            System.out.println("The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            System.out.println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");

        } else {

            Toast.makeText(UserProfileActivity.this, "UNDER DEVELOPMENT", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveOrderInCollection() {
        profileProgressBar.setVisibility(View.VISIBLE);
        ordersColReference.document(OneApplication.finalOrder.getOrderID()).set(OneApplication.finalOrder)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DisplayUtils.showToast(UserProfileActivity.this, "Order Placed", Toast.LENGTH_SHORT);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            profileProgressBar.setVisibility(View.INVISIBLE);
                            DisplayUtils.showToast(UserProfileActivity.this, "Problem Placing Order", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }


}

