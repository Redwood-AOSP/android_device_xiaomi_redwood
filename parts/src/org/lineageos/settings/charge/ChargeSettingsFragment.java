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

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreferenceCompat;

import org.lineageos.settings.R;

public class ChargeSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_BYPASS_CHARGE = "bypass_charge";

    private ChargeUtils chargeUtils;
    private SwitchPreferenceCompat bypassChargePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.charge_settings, rootKey);

        chargeUtils = new ChargeUtils(getActivity());
        bypassChargePreference = findPreference(KEY_BYPASS_CHARGE);

        boolean supported = chargeUtils.isBypassChargeSupported();
        if (bypassChargePreference == null) {
            return;
        }

        bypassChargePreference.setEnabled(supported);
        if (supported) {
            bypassChargePreference.setChecked(chargeUtils.isBypassChargeEnabled());
            bypassChargePreference.setOnPreferenceChangeListener(this);
        } else {
            bypassChargePreference.setSummary(R.string.charge_bypass_unavailable);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!KEY_BYPASS_CHARGE.equals(preference.getKey())) {
            return false;
        }

        boolean enable = (Boolean) newValue;
        if (!enable) {
            chargeUtils.enableBypassCharge(false);
            BypassChargeTileService.updateTile(getActivity());
            return true;
        }

        ChargeUtils.SafetyCheckResult safetyCheck = chargeUtils.performSafetyChecks();
        if (!safetyCheck.isSafe()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.charge_bypass_title)
                    .setMessage(getString(R.string.charge_bypass_safety_failed,
                            safetyCheck.getReason()))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return false;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.charge_bypass_title)
                .setMessage(R.string.charge_bypass_warning)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    chargeUtils.enableBypassCharge(true);
                    bypassChargePreference.setChecked(true);
                    BypassChargeTileService.updateTile(getActivity());
                })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> bypassChargePreference.setChecked(false))
                .show();
        return false;
    }
}
