package com.son.alarmclock.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.*;
import com.son.alarmclock.QuizPref;
import com.son.alarmclock.R;
import com.son.alarmclock.activities.InPurchaseActivity;
import com.son.alarmclock.activities.MainActivity;
import com.son.alarmclock.databinding.FragmentCreateAlarmBinding;
import com.son.alarmclock.model.Alarm;
import com.son.alarmclock.util.DayUtil;
import com.son.alarmclock.util.TimePickerUtil;
import com.son.alarmclock.viewmodel.CreateAlarmViewModel;
import org.jetbrains.annotations.NotNull;


import java.util.*;

import static com.son.alarmclock.App.quizPref;

public class CreateAlarmFragment extends Fragment {
    FragmentCreateAlarmBinding fragmentCreateAlarmBinding;
    private CreateAlarmViewModel createAlarmViewModel;
    boolean isVibrate = false;
    String tone;
    private QuizPref quizPref;
    Alarm alarm;
    Ringtone ringtone;

    public CreateAlarmFragment() {
        // Required empty public constructor
    }

    Context context;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            alarm = (Alarm) getArguments().getSerializable(getString(R.string.arg_alarm_obj));
        }
        createAlarmViewModel = new ViewModelProvider(this).get(CreateAlarmViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCreateAlarmBinding = FragmentCreateAlarmBinding.inflate(inflater, container, false);
        View v = fragmentCreateAlarmBinding.getRoot();
        quizPref = QuizPref.getInstance();
        setupIAPOnCreate();
        tone = RingtoneManager.getActualDefaultRingtoneUri(this.getContext(), RingtoneManager.TYPE_ALARM).toString();
        ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(tone));
        fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(ringtone.getTitle(getContext()));
        if (alarm != null) {
            updateAlarmInfo(alarm);
        }
        fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.VISIBLE);
                } else {
                    fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.GONE);
                }
            }
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmScheduleAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarm != null) {
                    updateAlarm();
                } else {
                    scheduleAlarm();
                }

                Navigation.findNavController(v).navigate(R.id.action_createAlarmFragment_to_alarmsListFragment);
            }
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmCardSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) Uri.parse(tone));
                startActivityForResult(intent, 5);
            }
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isVibrate = true;
                } else {
                    isVibrate = false;
                }
            }
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                fragmentCreateAlarmBinding.fragmentCreatealarmScheduleAlarmHeading
                        .setText(DayUtil.getDay(TimePickerUtil.getTimePickerHour(timePicker), TimePickerUtil.getTimePickerMinute(timePicker)));
            }
        });

        return v;
    }

    private void scheduleAlarm() {
        String alarmTitle = getString(R.string.alarm_title);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString().isEmpty()) {
            alarmTitle = fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString();
        }
        Alarm alarm = new Alarm(
                alarmId,
                TimePickerUtil.getTimePickerHour(fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker),
                TimePickerUtil.getTimePickerMinute(fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker),
                alarmTitle,
                true,
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.isChecked(),
                tone,
                isVibrate
        );

        if (quizPref.totalCount() >= 1) {
            quizPref.giamPlayers(1);
            createAlarmViewModel.insert(alarm);
            alarm.schedule(getContext());
        } else {
            Toast.makeText(requireContext(), "Please buy more item", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                if (context != null){

                    context.startActivity(new Intent(context, InPurchaseActivity.class));
                }
            }, 1000);
        }

    }

    private void updateAlarm() {
        String alarmTitle = getString(R.string.alarm_title);
//        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString().isEmpty()) {
            alarmTitle = fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString();
        }
        Alarm updatedAlarm = new Alarm(
                alarm.getAlarmId(),
                TimePickerUtil.getTimePickerHour(fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker),
                TimePickerUtil.getTimePickerMinute(fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker),
                alarmTitle,
                true,
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.isChecked(),
                tone,
                isVibrate
        );
        createAlarmViewModel.update(updatedAlarm);
        updatedAlarm.schedule(getContext());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            ringtone = RingtoneManager.getRingtone(getContext(), uri);
            String title = ringtone.getTitle(getContext());
            if (uri != null) {
                tone = uri.toString();
                if (title != null && !title.isEmpty())
                    fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(title);
            } else {
                fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText("");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateAlarmInfo(Alarm alarm) {
        fragmentCreateAlarmBinding.fragmentCreatealarmTitle.setText(alarm.getTitle());
        fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker.setHour(alarm.getHour());
        fragmentCreateAlarmBinding.fragmentCreatealarmTimePicker.setMinute(alarm.getMinute());
        if (alarm.isRecurring()) {
            fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.setChecked(true);
            fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.VISIBLE);
            if (alarm.isMonday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.setChecked(true);
            if (alarm.isTuesday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.setChecked(true);
            if (alarm.isWednesday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.setChecked(true);
            if (alarm.isThursday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.setChecked(true);
            if (alarm.isFriday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.setChecked(true);
            if (alarm.isSaturday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.setChecked(true);
            if (alarm.isSunday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.setChecked(true);
            tone = alarm.getTone();
            ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(tone));
            fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(ringtone.getTitle(getContext()));
            if (alarm.isVibrate())
                fragmentCreateAlarmBinding.fragmentCreatealarmVibrateSwitch.setChecked(true);
        }
    }


    private void setupIAPOnCreate() {
        PurchasingListener purchasingListener = new PurchasingListener() {
            @Override
            public void onUserDataResponse(UserDataResponse response) {
                switch (response.getRequestStatus()) {
                    case SUCCESSFUL: {
                        quizPref.currentUserId(response.getUserData().getUserId());
                    }
                    case FAILED: {
                    }
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
                            products.get(key);
                        }
                        for (String s : productDataResponse.getUnavailableSkus()) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s");
                        }
                        break;
                    case FAILED:
                        Log.v("FAILED", "FAILED");
                    case NOT_SUPPORTED:
                }
            }

            @Override
            public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
                switch (purchaseResponse.getRequestStatus()) {
                    case SUCCESSFUL:
                        if (Objects.equals(purchaseResponse.getReceipt().getSku(), "com.son.alarmclock.level.01")) {
                            quizPref.countPlayers(5);
                        }
                        if (Objects.equals(purchaseResponse.getReceipt().getSku(), "com.son.alarmclock.level.02")) {
                            quizPref.countPlayers(10);
                        }
                        if (Objects.equals(purchaseResponse.getReceipt().getSku(), "com.son.alarmclock.level.04")) {
                            quizPref.countPlayers(15);
                        }
                        PurchasingService.notifyFulfillment(
                                purchaseResponse.getReceipt().getReceiptId(),
                                FulfillmentResult.FULFILLED
                        );
                        Log.v("FAILED", "FAILED");
                    case FAILED: {
                    }
                    case INVALID_SKU: {
                    }
                    case ALREADY_PURCHASED: {
                    }
                    case NOT_SUPPORTED: {
                    }
                }
            }

            @Override
            public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
                switch (purchaseUpdatesResponse.getRequestStatus()) {
                    case SUCCESSFUL:
                        for (Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                            if (!receipt.isCanceled()) {
                                Log.d("onPurchaseUpdates", "xxxx1");
                            } else {
                                Log.d("onPurchaseUpdates", "xxxx2");
                            }
                        }
                        if (purchaseUpdatesResponse.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false);
                        }

                    case FAILED:
                        Log.d("FAILED", "FAILED");
                    case NOT_SUPPORTED: {
                    }
                }
            }
        };
        PurchasingService.registerListener(requireContext(), purchasingListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        PurchasingService.getUserData();
        PurchasingService.getPurchaseUpdates(false);
        Set<String> productSkus = new HashSet<>();
        productSkus.add("com.son.alarmclock.level.01");
        productSkus.add("com.son.alarmclock.level.02");
        productSkus.add("com.son.alarmclock.level.04");
        PurchasingService.getProductData(productSkus);
    }

    @Override
    public void onDestroyView() {
        fragmentCreateAlarmBinding = null;
//        context = null;
        super.onDestroyView();
    }
}