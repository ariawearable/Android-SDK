package com.deus_tech.testclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Test Activity that shows how to connect AriaService from your application
 */
public class MainActivity extends WearableActivity {

	//
	// AriaService constants
	//

	public static final String ARIA_PACKAGE_NAME = "com.deus_tech.aria";
	public static final String ARIA_SERVICE_NAME = "com.deus_tech.aria.AriaService";

	public static final int ARIA_MSG_REGISTER_CLIENT = 1;
	public static final int ARIA_MSG_UNREGISTER_CLIENT = 2;
	public static final int ARIA_MSG_GESTURE = 3;

	public final static int ARIA_GESTURE_HOME = 1;
	public final static int ARIA_GESTURE_ENTER = 2;
	public final static int ARIA_GESTURE_BACK = 3;
	public final static int ARIA_GESTURE_UP = 4;
	public final static int ARIA_GESTURE_DOWN = 5;

	/**
	 * A text view where to show gestures
	 */
	@Bind(R.id.tvTest)
	public TextView tvTest;

	/**
	 * Messenger for communicating with AriaService
	 */
	private Messenger mAriaService = null;

	/**
	 * Messenger object used by AriaService to send messages to your app
	 */
	private final Messenger mMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				// A gesture message
				// AriaService puts payload to msg.arg1
				case ARIA_MSG_GESTURE:
					tvTest.setText(
							getGestureName(msg.arg1)
					);
					break;
				default:
					super.handleMessage(msg);
			}
		}
	});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init UI layout
	    setContentView(R.layout.activity_main);
	    ButterKnife.bind(this);
    }

	@Override
	protected void onStart() {
		super.onStart();

		// Bind AriaService that's running in another process
		// You can do multiple bindings in any of your Activities or Services
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(ARIA_PACKAGE_NAME, ARIA_SERVICE_NAME));
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Unregister AriaService client first before unbind (required)
		ariaUnregisterClient();

		// Unbind from AriaService
		unbindService(mConnection);
	}

	/**
	 * Standard Android object interacting with services
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		/**
		 * This is called when the connection with the service has been established
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			switch (className.getClassName()) {
				// You can have multiple service in your app
				// So we check if this is AriaService
				case ARIA_SERVICE_NAME:
					// We are communicating with our service through an IDL interface,
					// so get a client-side representation of that from the raw service object.
					mAriaService = new Messenger(service);
					// Register AriaService client to receive gesture messages
					ariaRegisterClient();
					break;

				// Check your connected services here
			}
		}

		/**
		 * This is called when the connection with the service has been unexpectedly disconnected
		 */
		public void onServiceDisconnected(ComponentName className) {
			switch (className.getClassName()) {
				case ARIA_SERVICE_NAME:
					ariaUnregisterClient();
					mAriaService = null;
					break;

				// Check your disconnected services here
			}
		}
	};

	private void ariaRegisterClient() {
		try {
			// Send a registration message to AriaService
			// AriaService will send you gestures until you unregister
			Message msg = Message.obtain(null, ARIA_MSG_REGISTER_CLIENT);
			msg.replyTo = mMessenger;
			mAriaService.send(msg);

			// Show user we are connected to service
			Toast.makeText(MainActivity.this, "RPC connected", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "RPC error", Toast.LENGTH_SHORT).show();
			e.getStackTrace();
		}
	}

	private void ariaUnregisterClient() {
		try {
			// Try to send undegister message
			Message msg = Message.obtain(null, ARIA_MSG_UNREGISTER_CLIENT);
			msg.replyTo = mMessenger;
			mAriaService.send(msg);

			// Show user we are disconnected to service
			Toast.makeText(MainActivity.this, "RPC disconnected", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// Connection to service was lost
			e.getStackTrace();
		}
	}

	/**
	 * Resolves gesture number into a text
	 */
	private String getGestureName(int gesture) {
		String gestureString = "";
        if (gesture == ARIA_GESTURE_HOME) {
            gestureString = "Home";
        } else if (gesture == ARIA_GESTURE_ENTER) {
            gestureString = "Enter";
        } else if (gesture == ARIA_GESTURE_BACK) {
            gestureString = "Back";
        } else if (gesture == ARIA_GESTURE_UP) {
            gestureString = "Up";
        } else if (gesture == ARIA_GESTURE_DOWN) {
            gestureString = "Down";
        }
		return gestureString;
	}

}
