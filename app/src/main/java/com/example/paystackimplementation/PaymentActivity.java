package com.example.paystackimplementation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class PaymentActivity extends AppCompatActivity {

    private Card card;
//    private Charge charge;
    private EditText emailField;
    private EditText cardNumberField;
    private EditText expiryMonthField;
    private EditText expiryYearField;
    private EditText cvvField;

    private String email, cardNumber, cvv;
    private int expiryMonth, expiryYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PaystackSdk.initialize(getApplicationContext());
        setContentView(R.layout.activity_payment);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();


        Button payBtn = findViewById(R.id.button_pay);
//        Button cancelBtn = findViewById(R.id.button_cancel);

        emailField = findViewById(R.id.edit_email_address);
        cardNumberField = findViewById(R.id.edit_card_number);
        expiryMonthField = findViewById(R.id.edit_expiry_month);
        expiryYearField = findViewById(R.id.edit_expiry_year);
        cvvField = findViewById(R.id.edit_cvc);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
//                    Toast.makeText(PaymentActivity.this, "Good", Toast.LENGTH_SHORT).show();
//                    return;
                    performCharge();
                    try {
                        email = emailField.getText().toString().trim();
                        cardNumber = cardNumberField.getText().toString().trim();
                        expiryMonth = Integer.parseInt(expiryMonthField.getText().toString().trim());
                        expiryYear = Integer.parseInt(expiryYearField.getText().toString().trim());
                        cvv = cvvField.getText().toString().trim();

                        //String cardNumber = "4084084084084081";
                        //int expiryMonth = 11; //any month in the future
                        //int expiryYear = 18; // any year in the future
                        //String cvv = "408";
                        card = new Card(cardNumber, expiryMonth, expiryYear,cvv, email); //add email as a parameter
                        if (card.isValid()) {
                            Toast.makeText(PaymentActivity.this, "Done", Toast.LENGTH_SHORT).show();
//                            performCharge();
                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
//                    return;
                }
                else {
                    Toast.makeText(PaymentActivity.this, "Card Not Valid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    Method to perform the charging of the card
    private void performCharge(){
        final ProgressDialog progressDialog = new ProgressDialog(PaymentActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating Payment...");
        progressDialog.show();
//        create a charge object
        Charge charge = new Charge();

//        set the card to charge
        charge.setCard(card);

//        call this method if you set a plan
//        charge.setPlan("PLN_yourplan");

        charge.setAmount(20000); // amount in kobo

        charge.setEmail(emailField.getText().toString().trim());

        PaystackSdk.chargeCard(PaymentActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                progressDialog.dismiss();
//                this is called only after transaction is deemed successful
//                retrieve the transaction and send its reference to your server
//                for verification
                String paymentReference = transaction.getReference();
                Toast.makeText(PaymentActivity.this, "Transaction Successful!! payment reference: " + paymentReference, Toast.LENGTH_SHORT).show();

                DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null){

                    mReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("payment_status").setValue(true);
                }
                finish();
                startActivity(new Intent(PaymentActivity.this, SuccessfulActivity.class));
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                progressDialog.dismiss();
//                this is called only before requesting OTP
//                save reference so may send to server
//                if error occurs with OTP, you should still verify on server
                Toast.makeText(PaymentActivity.this, "Payment Process has started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                progressDialog.dismiss();
//                handle error here
                Toast.makeText(PaymentActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
//            error.getMessage()
        });
    }

    private boolean validateForm(){
        boolean valid = true;

        String cardNumber = cardNumberField.getText().toString();
        if (TextUtils.isEmpty(cardNumber)){
            cardNumberField.setError("Required");
            valid = false;
        }else {
            cardNumberField.setError(null);
        }
        String expiryMonth = expiryMonthField.getText().toString();
        if (TextUtils.isEmpty(expiryMonth)){
            expiryMonthField.setError("Required");
            valid = false;
        }else{
            expiryMonthField.setError(null);
        }
        String expiryYear = expiryYearField.getText().toString();
        if (TextUtils.isEmpty(expiryYear)){
            expiryYearField.setError("Required");
            valid= false;
        }else {
            expiryYearField.setError(null);
        }
        String cvv = cvvField.getText().toString();
        if (TextUtils.isEmpty(cvv)){
            cvvField.setError("Required");
            valid = false;
        }else {
            cvvField.setError(null);
        }
        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
