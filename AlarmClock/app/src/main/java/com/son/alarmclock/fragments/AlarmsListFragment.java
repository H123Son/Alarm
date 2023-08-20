package com.son.alarmclock.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amazon.device.drm.LicensingService;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.son.alarmclock.QuizPref;
import com.son.alarmclock.R;
import com.son.alarmclock.adapter.AlarmRecyclerViewAdapter;
import com.son.alarmclock.databinding.FragmentAlarmsListBinding;
import com.son.alarmclock.model.Alarm;
import com.son.alarmclock.util.OnToggleAlarmListener;
import com.son.alarmclock.viewmodel.AlarmListViewModel;
import com.son.app3.activities.UpToPremiumAct;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.son.alarmclock.App.quizPref;

public class AlarmsListFragment extends Fragment implements OnToggleAlarmListener {
    private AlarmRecyclerViewAdapter alarmRecyclerViewAdapter;
    private AlarmListViewModel alarmsListViewModel;
    private RecyclerView alarmsRecyclerView;
    private FloatingActionButton addAlarm;
    private FragmentAlarmsListBinding fragmentAlarmsListBinding;
    private String skuWeek = "com.son.alarmclock.weekpremium";
    private String skuMonth = "com.son.alarmclock.monthpremium";
    private String skuQuarter = "com.son.alarmclock.quarterpremium";
    private String currentUserID;
    private QuizPref quizPref;
    String currentMarketPlace;

    public AlarmsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmRecyclerViewAdapter = new AlarmRecyclerViewAdapter(this);
        alarmsListViewModel = new ViewModelProvider(this).get(AlarmListViewModel.class);
        alarmsListViewModel.getAlarmsLiveData().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                if (alarms != null) {
                    alarmRecyclerViewAdapter.setAlarms(alarms);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentAlarmsListBinding = FragmentAlarmsListBinding.inflate(inflater, container, false);
        View view = fragmentAlarmsListBinding.getRoot();
        setupIAPOnCreate();
        quizPref = QuizPref.getInstance();
        alarmsRecyclerView = fragmentAlarmsListBinding.fragmentListalarmsRecylerView;
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmsRecyclerView.setAdapter(alarmRecyclerViewAdapter);

        addAlarm = fragmentAlarmsListBinding.fragmentListalarmsAddAlarm;
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quizPref.isPremium()) {
                    Navigation.findNavController(v)
                            .navigate(R.id.action_alarmsListFragment_to_createAlarmFragment);
                } else {
                    Toast.makeText(getContext(), "Please up to premium for app!", Toast.LENGTH_SHORT).show();
                    new Handler().postAtTime(() -> {
                        startActivity(new Intent(requireContext(), UpToPremiumAct.class));
                    }, 3000);
                }

            }
        });

        return view;
    }

    @Override
    public void onToggle(Alarm alarm) {
        if (alarm.isStarted()) {
            alarm.cancelAlarm(getContext());
            alarmsListViewModel.update(alarm);
        } else {
            alarm.schedule(getContext());
            alarmsListViewModel.update(alarm);
        }
    }

    @Override
    public void onDelete(Alarm alarm) {
        if (alarm.isStarted())
            alarm.cancelAlarm(getContext());
        alarmsListViewModel.delete(alarm.getAlarmId());
    }

    @Override
    public void onItemClick(Alarm alarm, View view) {

        if (alarm.isStarted())
            alarm.cancelAlarm(getContext());
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.arg_alarm_obj), alarm);
        Navigation.findNavController(view).navigate(R.id.action_alarmsListFragment_to_createAlarmFragment, args);
    }

    @Override
    public void onStart() {
        super.onStart();
        Set<String> productSkus = new HashSet<>();
        productSkus.add(skuWeek);
        productSkus.add(skuMonth);
        productSkus.add(skuQuarter);
        PurchasingService.getProductData(productSkus);
    }

    private void setupIAPOnCreate() {
        PurchasingListener purchasingListener = new PurchasingListener() {
            @Override
            public void onUserDataResponse(UserDataResponse response) {
                switch (response.getRequestStatus()) {
                    case SUCCESSFUL:
                        currentUserID = response.getUserData().getUserId();
                        currentMarketPlace = response.getUserData().getMarketplace();
                        quizPref.currentUserId(currentUserID);
                        Log.v("IAP SDK", "loaded userdataResponse");

                    case FAILED:
                    case NOT_SUPPORTED:
                        Log.v("IAP SDK", "loading failed");

                }
            }

            @Override
            public void onProductDataResponse(ProductDataResponse productDataResponse) {
                switch (productDataResponse.getRequestStatus()) {
                    case SUCCESSFUL:
                        Map<String, Product> products = productDataResponse.getProductData();
                        for (String key : products.keySet()) {
                            Product p = products.get(key);
                            Log.v(
                                    "Product:",
                                    String.format(
                                            "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                            p.getTitle(),
                                            p.getProductType(),
                                            p.getSku(),
                                            p.getPrice(),
                                            p.getDescription()
                                    )
                            );
                        }
                        for (String s : productDataResponse.getUnavailableSkus()) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s");
                        }
                        break;

                    case FAILED:
                        Log.v("FAILED", "FAILED");

                }
            }

            @Override
            public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
                switch (purchaseResponse.getRequestStatus()) {
                    case SUCCESSFUL:
                        PurchasingService.notifyFulfillment(
                                purchaseResponse.getReceipt().getReceiptId(),
                                FulfillmentResult.FULFILLED
                        );
                        quizPref.setIsPremium(!purchaseResponse.getReceipt().isCanceled());
                        Log.v("FAILED", "FAILED");
                        break;
                    case FAILED: {
                    }
                }

            }

            @Override
            public void onPurchaseUpdatesResponse(@NonNull PurchaseUpdatesResponse purchaseUpdatesResponse) {
                switch (purchaseUpdatesResponse.getRequestStatus()) {
                    case SUCCESSFUL:
                        for (Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                            if (!receipt.isCanceled()) {
                                quizPref.setIsPremium(true);
                            } else {
                                quizPref.setIsPremium(false);
                            }
                        }
                        if (purchaseUpdatesResponse.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false);
                        }
                    case FAILED:
                        Log.d("FAILED", "FAILED");
                }
            }
        };
        PurchasingService.registerListener(requireContext(), purchasingListener);
        Log.d(
                "MainActivity",
                "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        PurchasingService.getUserData();
        PurchasingService.getPurchaseUpdates(false);
    }
}