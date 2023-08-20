package com.son.alarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class QuizPref {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static QuizPref quizPref;

    private QuizPref(Context context) {
        sharedPreferences = context.getSharedPreferences("shared", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void init(Context context) {
        if (quizPref == null) {
            quizPref = new QuizPref(context);
        }
    }

    public static QuizPref getInstance() {
        return quizPref;
    }

    public void saveName(String name) {
        editor.putString("name", name);
        editor.apply();
    }

    public void historyQuiz(String quizList) {
        editor.putString("historys", quizList);
        editor.apply();
    }

    public void showResultQuiz(String quizList) {
        editor.putString("showResultQuiz", quizList);
        editor.apply();
    }

    public String getShowResultQuiz() {
        return sharedPreferences
                .getString("showResultQuiz", "");
    }

    public String getHistoryQuiz() {
        return sharedPreferences.getString("historys", "");
    }

    public String getName() {
        return sharedPreferences
                .getString("name", "");
    }

    public int totalCount() {
        String userId = sharedPreferences.getString("UserId", "");
        int count = sharedPreferences.getInt("countstart_" + userId, 5);
        Log.i("SON", "totalCount: " + count);
        return count;
    }


    public void currentUserId(String userid) {
        editor.putString("UserId", userid);
        editor.apply();
    }

    public void setIsPremium(Boolean state) {
        String userId = sharedPreferences.getString("UserId", "");
        editor.putBoolean("PremiumPlan_$userId" + userId, state);
        editor.apply();
    }

    public Boolean isPremium() {
        String userId = sharedPreferences.getString("UserId", "");
        return sharedPreferences.getBoolean("PremiumPlan_$userId" + userId, false);
    }

    public void countPlayers(int step) {
        String userId = sharedPreferences.getString("UserId", "");
        int count = sharedPreferences.getInt("countstart_" + userId, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        count += step;
        editor.putInt("countstart_" + userId, count);
        editor.apply();
    }

    public void giamPlayers(int step) {
        String userId = sharedPreferences.getString("UserId", "");
        int count = sharedPreferences.getInt("countstart_" + userId, 5);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        count--;
        Log.i("SON", "giamPlayers: " + count);
        editor.putInt("countstart_" + userId, count);
        editor.apply();
    }
}
