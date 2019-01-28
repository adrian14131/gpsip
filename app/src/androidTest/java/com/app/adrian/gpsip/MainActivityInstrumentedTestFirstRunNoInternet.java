package com.app.adrian.gpsip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class MainActivityInstrumentedTestFirstRunNoInternet {

    Intent intent = new Intent();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class, true, false);
    MainActivity mainActivity;
    String TAG = "GPSip (TEST) class: "+this.getClass().getSimpleName();

    @Before
    public void setUp() throws Exception {
        intent.putExtra("mode", "Test");
        intent.putExtra("networkInTest", false);
        intent.putExtra("inInstrumentedTest", true);

        mActivityRule.launchActivity(intent);
        mainActivity = mActivityRule.getActivity();
        /*mainActivity.inInstrumentedTest = true;
        mainActivity.networkInTest = false;*/
        deleteFiles();

    }

    public void deleteFiles(){
        mainActivity.lineJsonFile.delete();
        mainActivity.stopsJsonFile.delete();
        mainActivity.stopsModifyJsonFile.delete();
        mainActivity.stopsPointJsonFile.delete();
        mainActivity.stopsPointModifyJsonFile.delete();
    }

    @Test
    public void test2_noDataNoInternetClickNo(){

        onView(withId(R.id.infoLabel)).check(matches(withText("Brak danych")));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertFalse(mainActivity.downloadedCorrectly());
        onView(withId(R.id.previousButton)).check(matches(not(isEnabled())));
        onView(withId(R.id.nextButton)).check(matches(not(isEnabled())));
        onView(withId(R.id.stopButton)).check(matches(isEnabled()));
        onView(withId(R.id.stopButton)).check(matches(withText("wyjdź")));


    }
}