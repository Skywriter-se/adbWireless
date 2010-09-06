package siir.es.adbWireless;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class adbWireless extends Activity {

	public static final String MSG_TAG = "ADBWIRELESS";
	public static final String PORT = "5555";

	private WifiManager mWifiManager;
	private NotificationManager mNotificationManager;

	private static boolean mState = false;
	private boolean wifiState;

	private TextView tv_footer_1;
	private TextView tv_footer_2;
	private TextView tv_footer_3;
	private ImageView iv_button;

	private static final int MENU_PREFERENCES = 1;
	private static final int MENU_ABOUT = 2;
	private static final int MENU_EXIT = 3;

	private static final int START_NOTIFICATION_ID = 1;
	private static final int ACTIVITY_SETTINGS = 2;

	ProgressDialog spinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		spinner = new ProgressDialog(adbWireless.this);

		this.iv_button = (ImageView) findViewById(R.id.iv_button);

		this.tv_footer_1 = (TextView) findViewById(R.id.tv_footer_1);
		this.tv_footer_2 = (TextView) findViewById(R.id.tv_footer_2);
		this.tv_footer_3 = (TextView) findViewById(R.id.tv_footer_3);

		this.mWifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		this.mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// SharedPreferences settings = getSharedPreferences("wireless", 0);
		// mState = settings.getBoolean("mState", false);

		// updateState();

		if (!hasRootPermission()) {
			// Log.d(MSG_TAG, "Not Root!");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.no_root))
					.setCancelable(true)
					.setPositiveButton(getString(R.string.button_close),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									adbWireless.this.finish();
								}
							});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.create();
			builder.setTitle(R.string.no_root_title);
			builder.show();
		}

		if (!checkWifiState()) {
			// Log.d(MSG_TAG, "Not Wifi!");
			wifiState=false;
			saveWiFiState(wifiState);
			
			if (prefsWiFiOn()) {
				enableWiFi(true);
			} else {
				WiFidialog();
			}
		}else {
			wifiState=true;
			saveWiFiState(wifiState);
		}

		this.iv_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				if (prefsHaptic())
					vib.vibrate(35);

				try {
					if (!mState) {
						spinner.setMessage(getString(R.string.Turning_on));
						spinner.show();

						adbStart();

					} else {
						spinner.setMessage(getString(R.string.Turning_off));
						spinner.show();
						adbStop();

					}

					updateState();
					spinner.cancel();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

	private void saveWiFiState(boolean wifiState){
		
		SharedPreferences settings = getSharedPreferences("wireless", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("wifiState", wifiState);
		editor.commit();
	}
	
	private void WiFidialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.no_wifi))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.button_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								adbWireless.this.finish();
							}
						})
				.setNegativeButton(R.string.button_activate_wifi,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								enableWiFi(true);
								dialog.cancel();

							}
						});
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.create();
		builder.setTitle(R.string.no_wifi_title);
		builder.show();

	}

	@Override
	protected void onResume() {
		SharedPreferences settings = getSharedPreferences("wireless", 0);
		mState = settings.getBoolean("mState", false);
		wifiState = settings.getBoolean("wifiState", false);
		
		updateState();
		super.onResume();
	}



	@Override
	protected void onDestroy() {
		
			if (prefsWiFiOff() && !wifiState && checkWifiState()){
				enableWiFi(false);
			}
				
			try {
				adbStop();
				
			} catch (Exception e) {
			}

			try {
				mNotificationManager.cancelAll();
			} catch (Exception e) {
			}
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_PREFERENCES, 0, R.string.menu_prefs).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, MENU_EXIT, 0, R.string.menu_exit).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			Intent i = new Intent(this, ManagePreferences.class);
			startActivityForResult(i, ACTIVITY_SETTINGS);
			break;
		case MENU_ABOUT:
			showHelpDialog();
			return true;
		case MENU_EXIT:
			adbWireless.this.finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void updateState() {
		if (mState) {
			tv_footer_1.setText(R.string.footer_text_1);
			try {
				tv_footer_2.setText("adb connect " + getWifiIp(mWifiManager)
						+ ":" + PORT);
			} catch (Exception e) {
				tv_footer_2.setText("adb connect unknowip:" + PORT);
			}
			tv_footer_2.setVisibility(View.VISIBLE);
			tv_footer_3.setVisibility(View.VISIBLE);
			iv_button.setImageResource(R.drawable.bt_off);

		} else {
			tv_footer_1.setText(R.string.footer_text_off);
			tv_footer_2.setVisibility(View.INVISIBLE);
			tv_footer_3.setVisibility(View.INVISIBLE);
			iv_button.setImageResource(R.drawable.bt_on);

		}

	}

	private boolean adbStart() {

		// Log.d(MSG_TAG, "adbStart()");
		try {

			setProp("service.adb.tcp.port", PORT);
			if (isProcessRunning("adbd")) {
				runRootCommand("stop adbd");
			}
			runRootCommand("start adbd");
			mState = true;
			SharedPreferences settings = getSharedPreferences("wireless", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("mState", mState);
			editor.commit();

			if (prefsNoti())
				showNotification(R.drawable.mini_wireless,
						getString(R.string.noti_text));

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean adbStop() throws Exception {

		// Log.d(MSG_TAG, "adbStop()");
		try {
			setProp("service.adb.tcp.port", "-1");
			runRootCommand("stop adbd");
			runRootCommand("start adbd");
			mState = false;
			SharedPreferences settings = getSharedPreferences("wireless", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("mState", mState);
			editor.commit();
			mNotificationManager.cancelAll();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static boolean isProcessRunning(String processName)
			throws Exception {
		// Log.d(MSG_TAG, "isProcessRunning("+processName+")");
		boolean running = false;
		Process process = null;
		process = Runtime.getRuntime().exec("ps");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.contains(processName)) {
				running = true;
				break;
			}
		}
		in.close();
		process.waitFor();
		return running;
	}

	private static boolean hasRootPermission() {
		// Log.d(MSG_TAG, "hasRootPermission()");
		Process process = null;
		DataOutputStream os = null;
		boolean rooted = true;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			if (process.exitValue() != 0) {
				rooted = false;
			}
		} catch (Exception e) {
			Log.d(MSG_TAG, "hasRootPermission error: " + e.getMessage());
			rooted = false;
		} finally {
			if (os != null) {
				try {
					os.close();
					process.destroy();
				} catch (Exception e) {
					// nothing
				}
			}
		}
		return rooted;
	}

	private static boolean runRootCommand(String command) {
		// Log.d(MSG_TAG, "runRootCommand("+command+")");
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Log.d(MSG_TAG,
					"Unexpected error - Here is what I know: " + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		return true;
	}

	private static boolean setProp(String property, String value) {
		// Log.d(MSG_TAG, "setProp("+property+","+value+")");
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("setprop " + property + " " + value + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

	private String getWifiIp(WifiManager wifiManager) {
		int ip = wifiManager.getConnectionInfo().getIpAddress();
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
				+ ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
	}

	private void enableWiFi(boolean enable) {
		
		if (enable) {
			Toast.makeText(getBaseContext(), R.string.Turning_on_wifi,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getBaseContext(), R.string.Turning_off_wifi,
					Toast.LENGTH_LONG).show();
		}
		mWifiManager.setWifiEnabled(enable);
	}

	private boolean checkWifiState() {
		try {
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			if (!mWifiManager.isWifiEnabled() || wifiInfo.getSSID() == null) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void showNotification(int icon, String text) {
		final Notification notifyDetails = new Notification(icon, text,
				System.currentTimeMillis());

		if (prefsSound())
			notifyDetails.defaults |= Notification.DEFAULT_SOUND;
		if (prefsVibrate())
			notifyDetails.defaults |= Notification.DEFAULT_VIBRATE;
		Intent notifyIntent = new Intent(getApplicationContext(),
				adbWireless.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(
				getApplicationContext(), 0, notifyIntent, 0);
		notifyDetails.setLatestEventInfo(getApplicationContext(),
				getResources().getString(R.string.app_name), text, intent);
		mNotificationManager.notify(START_NOTIFICATION_ID, notifyDetails);

	}

	private void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.about))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.button_close),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});
		builder.setIcon(R.drawable.icon);
		builder.create();
		builder.setTitle(R.string.app_name);
		builder.show();
	}

	private boolean prefsVibrate() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(
				getResources().getString(R.string.pref_vibrate_key), false);
	}

	private boolean prefsSound() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(getResources()
				.getString(R.string.pref_sound_key), false);
	}

	private boolean prefsNoti() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(
				getResources().getString(R.string.pref_noti_key), false);
	}

	private boolean prefsHaptic() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(
				getResources().getString(R.string.pref_haptic_key), false);
	}

	private boolean prefsWiFiOn() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(
				getResources().getString(R.string.pref_wifi_on_key), false);
	}

	private boolean prefsWiFiOff() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return pref.getBoolean(
				getResources().getString(R.string.pref_wifi_off_key), false);
		
	}

}