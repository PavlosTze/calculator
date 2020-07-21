/*
Author: Pavlos Tzegiannakis
The donation-related activity & functionality
*/
package com.tzegian.Calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.tzegian.Calculator.MainActivity.mFirebaseAnalytics;

public class DonationActivity extends AppCompatActivity {

    private List<SkuDetails> mProducts;
    private BillingClient mBillingClient;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        mProducts = new ArrayList<>();
        mActivity = this;

        PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
                Bundle bundle = new Bundle();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    mFirebaseAnalytics.logEvent("donationCompletedSuccess" , bundle);

                    Toast.makeText(mActivity, getString(R.string.success_donation), Toast.LENGTH_LONG).show();
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    mFirebaseAnalytics.logEvent("donationCompletedCancel" , bundle);
                    Toast.makeText(mActivity, getString(R.string.user_canceled_donation), Toast.LENGTH_LONG).show();
                } else {
                    mFirebaseAnalytics.logEvent("donationCompletedError" , bundle);
                    Toast.makeText(mActivity, getString(R.string.error_donation), Toast.LENGTH_LONG).show();
                }
            }
        };

        mBillingClient = BillingClient.newBuilder(this)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>();
                    skuList.add("donation_050");
                    skuList.add("donation_100");
                    skuList.add("donation_200");
                    skuList.add("donation_500");
                    skuList.add("donation_1000");
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    mBillingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    mProducts = skuDetailsList;
                                    showDonationMethods();
                                }
                            });

                }
            }
            @Override
            public void onBillingServiceDisconnected() {
            }
        });

        Bundle bundle = new Bundle();
        if(mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent("donationView" , bundle);
        }
    }

    @Override
    protected void onResume() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();

        if(purchases != null && !(purchases.isEmpty())) {
            for(Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }

        super.onResume();
    }

    /*
        Function that makes the purchases consumed, so a user can donate again the same amount
     */
    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
            }
        };

        mBillingClient.consumeAsync(consumeParams, listener);
    }

    static class sortByPrice implements Comparator<SkuDetails>
    {
        public int compare(SkuDetails a, SkuDetails b)
        {
            return (int)(a.getPriceAmountMicros() - b.getPriceAmountMicros());
        }
    }

    private void showDonationMethods() {
        ListView donationMethods = findViewById(R.id.donationMethods);

        ArrayList<String> productsToShow = new ArrayList<>();

        Collections.sort(mProducts, new sortByPrice());

        for(SkuDetails product : mProducts) {
            productsToShow.add(product.getPrice() + " - " + product.getDescription());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_donation, productsToShow);
        donationMethods.setAdapter(arrayAdapter);

        donationMethods.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                Bundle bundle = new Bundle();
                mFirebaseAnalytics.logEvent("donationInitiated" , bundle);

                SkuDetails currentSkuClicked = mProducts.get(position);
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(currentSkuClicked)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(mActivity, billingFlowParams).getResponseCode();

                if (responseCode != BillingClient.BillingResponseCode.OK) {
                    mFirebaseAnalytics.logEvent("donationErrorOnInitiated" , bundle);
                    Toast.makeText(mActivity, getString(R.string.error_donation), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
