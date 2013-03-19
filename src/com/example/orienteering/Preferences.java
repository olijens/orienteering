package com.example.orienteering;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Preferences extends PreferenceActivity {
		@Override
		public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				addPreferencesFromResource(R.xml.preferences);
		}
}
