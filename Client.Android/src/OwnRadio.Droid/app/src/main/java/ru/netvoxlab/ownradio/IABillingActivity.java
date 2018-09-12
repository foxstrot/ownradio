package ru.netvoxlab.ownradio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

public class IABillingActivity extends AppCompatActivity {//implements IabBroadcastListener, OnClickListener {
	
	Toolbar toolbar;
	
	private class PurchaseListener extends EmptyRequestListener<Purchase> {
		// your code here
	}
	
	private class InventoryCallback implements Inventory.Callback {
		@Override
		public void onLoaded(Inventory.Products products) {
			products.get("subs").getSku("test_subscribe_1");
			// your code here
//			products.get("").
		}
	}
	
	private final ActivityCheckout mCheckout = Checkout.forActivity(this, App.get().getBilling());
	private Inventory mInventory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		//Меняем тему, используемую при запуске приложения, на основную
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_iabilling);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mCheckout.start();
		
		mCheckout.createPurchaseFlow(new PurchaseListener());
		
		mInventory = mCheckout.makeInventory();
		mInventory.load(Inventory.Request.create()
				.loadAllPurchases()
				.loadSkus(ProductTypes.SUBSCRIPTION, "test_subscribe_1"), new InventoryCallback());

		
		}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		mCheckout.stop();
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCheckout.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onSubscribeButtonClicked(View v) {
		mCheckout.whenReady(new Checkout.EmptyListener() {
			@Override
			public void onReady(BillingRequests requests) {
				if(mCheckout.isBillingSupported("test_subscribe_1"))
					requests.purchase(ProductTypes.SUBSCRIPTION, "test_subscribe_1", null, mCheckout.getPurchaseFlow());
			}
		});
	}
}

//	// Does the user have an active subscription?
//	boolean mSubscribed = false;
//
//	// Will the subscription auto-renew?
//	boolean mAutoRenewEnabled = false;
//
//
//	// Tracks the currently owned infinite SKU, and the options in the Manage dialog
//	String mInfiniteSku = "";
//	String mFirstChoiceSku = "";
//	String mSecondChoiceSku = "";
//
//	// Used to select between purchasing gas on a monthly or yearly basis
//	String mSelectedSubscriptionPeriod = "";
//
//
//	// SKU for our subscription
//	static final String SKU_SUBSCRIBE_ONE_MONTHLY = "subscribe_one_monthly";
//	static final String SKU_SUBSCRIBE_ONE_YEARLY = "subscribe_one_yearly";
//
//	// (arbitrary) request code for the purchase flow
//	static final int RC_REQUEST = 10001;
//
//	// The helper object
//	IabHelper mHelper;
//
//	// Provides purchase notification while this app is running
//	IabBroadcastReceiver mBroadcastReceiver;
//
//	PrefManager prefManager;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		//Меняем тему, используемую при запуске приложения, на основную
//		setTheme(R.style.AppTheme);
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_iabilling);
//
//		Button btnSubscribe = (Button) findViewById(R.id.btnSubscribe);
//
//		//prefManager = new PrefManager(getApplicationContext());
//		//prefManager.getPrefItemInt("subscribe_level", 0);
//
//
//
//		/* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
//         * (that you got from the Google Play developer console).
//         *
//         */
//		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyvzn+k5/eXZdtnHoO/zly7/hHZXVwihn6NRUCT06w597WJpT5GhTTi3SE8Ve4+kcaIAR20Au9bAje+XYWAtGckxNGK7q0RG5cBw05UEn0C/ZvWGunIIJw4rnMlMMu4Q3kNyHyvQhso3tVJCWdqdj3nZkj29MZJ6fU5EaNlxGEX5G7y6pvbevj8qikJhzIsKIxhVNwPfRPu5dalK0Ftan/7fzzsgq9DC3mh5AF6000I4oeocgBhlZQB4znOoJ9omWmcQ6PdUNGul8ny+TNYbQpl/aF9/09DsXlkHZSZ83bJYPsYUogw3AR356siyEOks3Dim5or8pC298NLH+XiNu6QIDAQAB";
//
//		// Create the helper, passing it our context and the public key to verify signatures with
//		Log.d(TAG, "Creating IAB helper.");
//		mHelper = new IabHelper(this, base64EncodedPublicKey);
//
//		// enable debug logging (for a production application, you should set this to false).
//		mHelper.enableDebugLogging(true);
//		// Start setup. This is asynchronous and the specified listener
//		// will be called once setup completes.
//		Log.d(TAG, "Starting setup.");
//		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//			public void onIabSetupFinished(IabResult result) {
//				Log.d(TAG, "Setup finished.");
//
//				if (!result.isSuccess()) {
//					// Oh noes, there was a problem.
//					Log.d(TAG, "Problem setting up in-app billing: " + result);
//					return;
//				}
//
//				// Have we been disposed of in the meantime? If so, quit.
//				if (mHelper == null) return;
//
//				// Important: Dynamically register for broadcast messages about updated purchases.
//				// We register the receiver here instead of as a <receiver> in the Manifest
//				// because we always call getPurchases() at startup, so therefore we can ignore
//				// any broadcasts sent while the app isn't running.
//				// Note: registering this listener in an Activity is a bad idea, but is done here
//				// because this is a SAMPLE. Regardless, the receiver must be registered after
//				// IabHelper is setup, but before first call to getPurchases().
//				mBroadcastReceiver = new IabBroadcastReceiver(IABillingActivity.this);
//				IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
//				registerReceiver(mBroadcastReceiver, broadcastFilter);
//
//				// IAB is fully set up. Now, let's get an inventory of stuff we own.
//				Log.d(TAG, "Setup successful. Querying inventory.");
//				try {
//					mHelper.queryInventoryAsync(mGotInventoryListener);
//				} catch (IabHelper.IabAsyncInProgressException e) {
//					Log.d(TAG, "Error querying inventory. Another async operation in progress.");
//				}
//			}
//		});
//	}
//
//	// Слушатель, который вызывается после завершения запроса элементов и подписки
//	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
//			Log.d(TAG, "Query inventory finished.");
//
//			if (mHelper == null) return;
//
//			if (result.isFailure()) {
//				Log.d(TAG, "Failed to query inventory: " + result);
//				return;
//			}
//
//			Log.d(TAG, "Query inventory was successful.");
//
//			// First find out which subscription is auto renewing
//			Purchase subscribeMonthly = inventory.getPurchase(SKU_SUBSCRIBE_ONE_MONTHLY);
//			Purchase subscribeYearly = inventory.getPurchase(SKU_SUBSCRIBE_ONE_YEARLY);
//			if (subscribeMonthly != null && subscribeMonthly.isAutoRenewing()) {
//				mInfiniteSku = SKU_SUBSCRIBE_ONE_MONTHLY;
//				mAutoRenewEnabled = true;
//			} else if (subscribeYearly != null && subscribeYearly.isAutoRenewing()) {
//				mInfiniteSku = SKU_SUBSCRIBE_ONE_YEARLY;
//				mAutoRenewEnabled = true;
//			} else {
//				mInfiniteSku = "";
//				mAutoRenewEnabled = false;
//			}
//
//			// The user is subscribed if either subscription exists, even if neither is auto
//			// renewing
//			mSubscribed = (subscribeMonthly != null && verifyDeveloperPayload(subscribeMonthly))
//					|| (subscribeYearly != null && verifyDeveloperPayload(subscribeYearly));
//			Log.d(TAG, "User " + (mSubscribed ? "HAS" : "DOES NOT HAVE")
//					+ " infinite subscription.");
//			if (mSubscribed){
//				// TODO: сохранение инфы о подписке, привести в соответствие кнопки
//			}
//
////			updateUi();
////			setWaitScreen(false);
//			Log.d(TAG, "Initial inventory query finished; enabling main UI.");
//		}
//	};
//
//	// "Subscribe to infinite" button clicked. Explain to user, then start purchase
//	// flow for subscription.
//	public void onSubscribeButtonClicked(View arg0) {
//		//TODO проверка и инициация подписки
//		if(!mHelper.subscriptionsSupported()) {
//			//// TODO: 21.08.2017 сообщение пользователю о том, что подписка не поддерживается на его устройстве
//			Log.d(TAG, "Subscription not supported on your device yet. Sorry!");
//			return;
//		}
//
//		CharSequence[] options;
//		if(!mSubscribed || !mAutoRenewEnabled) {
//			options = new CharSequence[2];
//			options[0] = getString(R.string.subscription_period_monthly);
//			options[1] = getString(R.string.subscription_period_yearly);
//			mFirstChoiceSku = SKU_SUBSCRIBE_ONE_MONTHLY;
//			mSecondChoiceSku = SKU_SUBSCRIBE_ONE_YEARLY;
//		} else {
//			// This is the subscription upgrade/downgrade path, so only one option is valid
//			options = new CharSequence[1];
//			if(mInfiniteSku.equals(SKU_SUBSCRIBE_ONE_MONTHLY)) {
//				options[0] = getString(R.string.subscription_period_monthly);
//				mFirstChoiceSku = SKU_SUBSCRIBE_ONE_MONTHLY;
//			} else {
//				options[0] = getString(R.string.subscription_period_yearly);
//				mFirstChoiceSku = SKU_SUBSCRIBE_ONE_YEARLY;
//			}
//			mSecondChoiceSku = "";
//		}
//
//		int titleResId;
//		if (!mSubscribed) {
//			titleResId = R.string.subscription_period_prompt;
//		} else if (!mAutoRenewEnabled) {
//			titleResId = R.string.subscription_resignup_prompt;
//		} else {
//			titleResId = R.string.subscription_update_prompt;
//		}
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(titleResId)
//				.setSingleChoiceItems(options, 0 /* checkedItem */, this)
//				.setPositiveButton(R.string.subscription_prompt_continue, this)
//				.setNegativeButton(R.string.subscription_prompt_cancel, this);
//		AlertDialog dialog = builder.create();
//		dialog.show();
//
//	}
//
//	@Override
//	public void onClick(DialogInterface dialog, int id) {
//		if (id == 0 /* First choice item */) {
//		} else if (id == 1 /* Second choice item */) {
//		} else if (id == DialogInterface.BUTTON_POSITIVE /* continue button */) {
//            /* TODO: for security, generate your payload here for verification. See the comments on
//             *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
//             *        an empty string, but on a production app you should carefully generate
//             *        this. */
//			String payload = "";
//
//			if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
//				// The user has not changed from the default selection
//				mSelectedSubscriptionPeriod = mFirstChoiceSku;
//			}
//
//			List<String> oldSkus = null;
//			if (!TextUtils.isEmpty(mInfiniteSku)
//					&& !mInfiniteSku.equals(mSelectedSubscriptionPeriod)) {
//				// The user currently has a valid subscription, any purchase action is going to
//				// replace that subscription
//				oldSkus = new ArrayList<String>();
//				oldSkus.add(mInfiniteSku);
//			}
//
////			setWaitScreen(true);
//			Log.d(TAG, "Launching purchase flow for subscription.");
//			try {
//				mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
//						oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
//			} catch (IabHelper.IabAsyncInProgressException e) {
//				Log.d(TAG, "Error launching purchase flow. Another async operation in progress.");
////				setWaitScreen(false);
//			}
//			// Reset the dialog options
//			mSelectedSubscriptionPeriod = "";
////			 = "";
//			mSecondChoiceSku = "";
//		} else if (id != DialogInterface.BUTTON_NEGATIVE) {
//			// There are only four buttons, this should not happen
//			Log.e(TAG, "Unknown button clicked in subscription dialog: " + id);
//		}
//	}
//
//	// Callback for when a purchase is finished
//	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
//		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
//
//
//		}
//	};
//
//
//	boolean verifyDeveloperPayload(Purchase p) {
//		String payload = p.getDeveloperPayload();
//
//        /*
//         * TODO: verify that the developer payload of the purchase is correct. It will be
//         * the same one that you sent when initiating the purchase.
//         *
//         * WARNING: Locally generating a random string when starting a purchase and
//         * verifying it here might seem like a good approach, but this will fail in the
//         * case where the user purchases an item on one device and then uses your app on
//         * a different device, because on the other device you will not have access to the
//         * random string you originally generated.
//         *
//         * So a good developer payload has these characteristics:
//         *
//         * 1. If two different users purchase an item, the payload is different between them,
//         *    so that one user's purchase can't be replayed to another user.
//         *
//         * 2. The payload must be such that you can verify it even when the app wasn't the
//         *    one who initiated the purchase flow (so that items purchased by the user on
//         *    one device work on other devices owned by the user).
//         *
//         * Using your own server to store and verify developer payloads across app
//         * installations is recommended.
//         */
//
//		return true;
//	}
//
//	@Override
//	public void receivedBroadcast() {
//		// Received a broadcast notification that the inventory of items has changed
//		Log.d(TAG, "Received broadcast notification. Querying inventory.");
//		try {
//			mHelper.queryInventoryAsync(mGotInventoryListener);
//		} catch (IabHelper.IabAsyncInProgressException e) {
//			Log.d(TAG, "Error querying inventory. Another async operation in progress.");
//		}
//	}
//
//
//	// We're being destroyed. It's important to dispose of the helper here!
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//
//		// very important:
//		if (mBroadcastReceiver != null) {
//			unregisterReceiver(mBroadcastReceiver);
//		}
//
//		// very important:
//		Log.d(TAG, "Destroying helper.");
//		if (mHelper != null) {
//			mHelper.disposeWhenFinished();
//			mHelper = null;
//		}
//	}
//}
