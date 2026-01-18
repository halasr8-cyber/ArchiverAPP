package com.example.archiver

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testArchivageScreenScenario() {

        // Vérifier que le bouton "Choisir un dossier" est affiché
        onView(withId(R.id.btnChooseFolder))
            .check(matches(isDisplayed()))

        //  Cliquer sur le bouton "Choisir un dossier"
        onView(withId(R.id.btnChooseFolder))
            .perform(click())

        //  Vérifier que le bouton "Lancer l’archivage" est affiché
        onView(withId(R.id.btnArchive))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // 4Vérifier que le switch "Archivage automatique" est affiché
        onView(withId(R.id.switchAutoArchive))
            .check(matches(isDisplayed()))
    }
}
