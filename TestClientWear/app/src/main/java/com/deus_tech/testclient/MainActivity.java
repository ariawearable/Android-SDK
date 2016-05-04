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

public class MainActivity extends WearableActivity {

	public static final String ARIA_PACKAGE_NAME = "com.deus_tech.aria";
	public static final String ARIA_SERVICE_NAME = "AriaService";


	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_GESTURE = 3;

	public final static int GESTURE_HOME = 1;
	public final static int GESTURE_ENTER = 2;
	public final static int GESTURE_BACK = 3;
	public final static int GESTURE_UP = 4;
	public final static int GESTURE_DOWN = 5;


	Messenger mService = null;
	boolean mIsBound;

	final Messenger mMessenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_GESTURE:
					tvTest.setText(getGestureName(msg.arg1));
					break;
				default:
					super.handleMessage(msg);
			}
		}
	});

	@Bind(R.id.tvTest)
    public TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setAmbientEnabled();
	    ButterKnife.bind(this);

	    Intent intent = new Intent();
	    intent.setComponent(new ComponentName("com.deus_tech.aria", "com.deus_tech.aria.ariaService.AriaService"));
	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
		                               IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
				// Give it some value as an example.
//				msg = Message.obtain(null,
//						MSG_SET_VALUE, this.hashCode(), 0);
//				mService.send(msg);
				Toast.makeText(MainActivity.this, "RPC connected", Toast.LENGTH_SHORT).show();
			} catch (RemoteException e) {
				Toast.makeText(MainActivity.this, "RPC error", Toast.LENGTH_SHORT).show();
				e.getStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			try {
				Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// connection lost
				e.getStackTrace();
			}

			mService = null;
			Toast.makeText(MainActivity.this, "RPC disconnected", Toast.LENGTH_SHORT).show();
		}
	};

	private String getGestureName(int gesture) {
		String gestureString = "";
        if (gesture == GESTURE_HOME) {
            gestureString = "Home";
        } else if (gesture == GESTURE_ENTER) {
            gestureString = "Enter";
        } else if (gesture == GESTURE_BACK) {
            gestureString = "Back";
        } else if (gesture == GESTURE_UP) {
            gestureString = "Up";
        } else if (gesture == GESTURE_DOWN) {
            gestureString = "Down";
        }
		return gestureString;
	}

}
