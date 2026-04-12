/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.charge;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

public class ChargeActivity extends CollapsingToolbarBaseActivity {

    private static final String TAG_BYPASS_CHARGE = "bypass_charge";
    private boolean observerRegistered;

    private final ContentObserver devOptionsObserver =
            new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    if (!isDeveloperOptionsEnabled()) {
                        new ChargeUtils(ChargeActivity.this).enableBypassCharge(false);
                        BypassChargeTileService.updateTile(ChargeActivity.this);
                        finish();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isDeveloperOptionsEnabled()) {
            finish();
            return;
        }

        getFragmentManager().beginTransaction().replace(
                com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                new ChargeSettingsFragment(),
                TAG_BYPASS_CHARGE
        ).commit();

        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED),
                false,
                devOptionsObserver
        );
        observerRegistered = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observerRegistered) {
            getContentResolver().unregisterContentObserver(devOptionsObserver);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDeveloperOptionsEnabled() {
        return Settings.Global.getInt(
                getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
    }
}
