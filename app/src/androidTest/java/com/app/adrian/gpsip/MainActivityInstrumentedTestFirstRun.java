package com.app.adrian.gpsip;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.transition.Transition;
import android.util.Log;
import android.widget.TextView;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.Statement;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class MainActivityInstrumentedTestFirstRun {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    MainActivity mainActivity;
    String TAG = "GPSip (TEST) class: "+this.getClass().getSimpleName();
    boolean networkInTest = true;


    @Before
    public void setUp() throws Exception {

        mainActivity = mActivityRule.getActivity();
        mainActivity.inInstrumentedTest = true;
        mainActivity.networkInTest = networkInTest;

    }

    public void deleteFiles(){
        mainActivity.lineJsonFile.delete();
        mainActivity.stopsJsonFile.delete();
        mainActivity.stopsModifyJsonFile.delete();
        mainActivity.stopsPointJsonFile.delete();
        mainActivity.stopsPointModifyJsonFile.delete();
    }
    @Test
    public void test1_AreFilesCreated(){
        assertTrue(mainActivity.areRequireFiles());
        deleteFiles();
        assertFalse(mainActivity.areRequireFiles());
        networkInTest = false;
    }
    @Test
    public void test2b_noDataClickNo(){
        System.out.println(TAG+mainActivity.networkIsActive()+" "+mainActivity.inInstrumentedTest);
        assertTrue("Connection error",mainActivity.networkIsActive());
        onView(withId(R.id.infoLabel)).check(matches(withText("Brak danych. Czy pobrać dane?")));
        assertTrue(mainActivity.downloadedCorrectly());
        assertTrue(mainActivity.isEmptyData());
        assertTrue(mainActivity.isUpdate());
        onView(withId(R.id.previousButton)).check(matches(isEnabled()));
        onView(withId(R.id.previousButton)).check(matches(withText("Tak")));
        onView(withId(R.id.nextButton)).check(matches(isEnabled()));
        onView(withId(R.id.nextButton)).check(matches(withText("Nie")));
        onView(withId(R.id.nextButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.infoLabel)).check(matches(withText("Brak danych")));
        onView(withId(R.id.stopButton)).check(matches(isEnabled()));
        onView(withId(R.id.stopButton)).check(matches(withText("wyjdź")));

    }

    @Test
    public void test2c_noDataClickYes(){
        assertTrue("Connection error",mainActivity.networkIsActive());
        onView(withId(R.id.infoLabel)).check(matches(withText("Brak danych. Czy pobrać dane?")));
        assertTrue(mainActivity.downloadedCorrectly());
        assertTrue(mainActivity.isEmptyData());
        assertTrue(mainActivity.isUpdate());
        onView(withId(R.id.previousButton)).check(matches(isEnabled()));
        onView(withId(R.id.previousButton)).check(matches(withText("Tak")));
        onView(withId(R.id.nextButton)).check(matches(isEnabled()));
        onView(withId(R.id.nextButton)).check(matches(withText("Nie")));
        onView(withId(R.id.previousButton)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TextView tv = mainActivity.findViewById(R.id.infoLabel);
        System.out.println(TAG+tv.getText().toString());
        onView(withId(R.id.infoLabel)).check(matches(withText("Wybierz linię")));
        assertFalse(mainActivity.isEmptyData());
    }
    @Test
    public void test3_toInfoLabel() {
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