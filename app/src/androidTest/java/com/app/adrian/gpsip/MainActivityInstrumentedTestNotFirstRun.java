package com.app.adrian.gpsip;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityInstrumentedTestNotFirstRun {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = mActivityRule.getActivity();
    }

    @Test
    public void areFilesCreated(){
        assertTrue(mainActivity.areRequireFiles());
    }

    @Test
    public void noDateClickNo(){

    }


    @Test
    public void toInfoLabel() {
        String testText1 = "Test 1";

        mainActivity.toInfoLabel(testText1);
        onView(withId(R.id.infoLabel)).check(matches(withText(testText1)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*@Test
    public void updateStopInfo() {
    }

    @Test
    public void updateDistance() {
    }*/
    /*@Test
    public void buttonEnabler() {

    }*/
}