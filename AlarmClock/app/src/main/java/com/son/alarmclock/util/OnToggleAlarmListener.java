package com.son.alarmclock.util;

import android.view.View;
import com.son.alarmclock.model.Alarm;


public interface OnToggleAlarmListener {
    void onToggle(Alarm alarm);
    void onDelete(Alarm alarm);
    void onItemClick(Alarm alarm,View view);
}
