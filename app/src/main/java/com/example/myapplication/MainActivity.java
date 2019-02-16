package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Fragment fragment = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    DrawerLayout drawerLayout;
    WebView mWebView;
    private Menu optionsMenu;
    Toolbar toolbar;
    NavigationView navigationView;
    AdView mAdView;
    InterstitialAd mInterstitialAd;
    ProgressBar progressBar;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    NestedScrollView nestedScrollView;
    final String url="https://google.com";

    final String admob_app_id = "ca-app-pub-3940256099942544~3347511713";
    final String admob_banner_id = "ca-app-pub-3940256099942544/6300978111";
    final String admob_inter_id = "ca-app-pub-3940256099942544/1033173712";
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewInit();

        setTitle("WebView");

        mWebView.loadUrl(url);

        setMySwipeRefreshLayout();

        setSupportActionBar(toolbar);

        //setmFirebaseAnalytics();

        floatingActionButton();

        setActionBarToogle();

        //setLocationPermission();

        oneSignalInit();

        //checkPermission();//storage

        webSettings();

        setAdmob();

        //setRTL();
    }

    void setRTL(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }


    final void setAdmob(){
        MobileAds.initialize(this, admob_app_id);


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(admob_banner_id);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(admob_inter_id);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        //  requestNewInterstitial(); //add test device
    }


    final void setMySwipeRefreshLayout(){
        mySwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    final public void onRefresh() {
                        mWebView.reload();
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }


   final void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B9C840C4E9AD8EC5D1497C9A62C56374")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    final void setLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 0);
    }

    final void setActionBarToogle(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    final void floatingActionButton(){
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            final public void onClick(View view) {
                share();
            }
        });
    }

    final void setmFirebaseAnalytics(){
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("class", "MainActivity");
        params.putString("userid", "12564578");
        firebaseAnalytics.logEvent("MainActivity", params);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);

    }


    @Override
    final public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebViewFragment inside Fragment
            // Use RESULT_OK only if you're implementing WebViewFragment inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    final void viewInit(){
        drawerLayout = findViewById(R.id.drawer_layout);
        mWebView = findViewById(R.id.mWebView);
        navigationView =  findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        frameLayout = findViewById(R.id.content_frame);
        progressBar = findViewById(R.id.progressBar);
        nestedScrollView = findViewById(R.id.nested);
    }

     void webSettings(){
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDescription,
                                        String mimetype, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                String fileName = URLUtil.guessFileName(url,contentDescription,mimetype);

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);

                DownloadManager dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                dManager.enqueue(request);

                Toasty.info(getApplicationContext(), R.string.download_info, Toast.LENGTH_SHORT, true).show();
            }
        });

        mWebView.setWebViewClient(new WebViewClient()
        {

            public void onReceivedError(WebView mWebView, int i, String s, String d1)
            {
                Toasty.error(getApplicationContext(),"No Internet Connection!").show();
                mWebView.loadUrl("file:///android_asset/net_error.html");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                boolean d = false;
                if(d==false){
                    nestedScrollView.scrollTo(0,0);
                    d=true;
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //opening link external browser
                /*if(!url.contains("android_asset")){
                    view.setWebViewClient(null);
                } else {
                    view.setWebViewClient(new WebViewClient());
                }*/

                if(url.contains("youtube.com") || url.contains("play.google.com") || url.contains("google.com/maps") || url.contains("facebook.com") || url.contains("twitter.com") || url.contains("instagram.com")){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                else if(url.startsWith("mailto")){
                    handleMailToLink(url);
                    return true;
                }
                else if(url.startsWith("tel:")){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                }
                else if(url.startsWith("sms:")){
                    // Handle the sms: link
                    handleSMSLink(url);

                    // Return true means, leave the current web view and handle the url itself
                    return true;
                }
                else if(url.contains("geo:")) {
                    Uri gmmIntentUri = Uri.parse(url);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

        });


        mWebView.setWebChromeClient(new WebChromeClient(){

            public Bitmap getDefaultVideoPoster()
            {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }

            public void onHideCustomView()
            {
                ((FrameLayout)getWindow().getDecorView()).removeView(mCustomView);
                mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
                setRequestedOrientation(mOriginalOrientation);
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
            }

            public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
            {
                if (mCustomView != null)
                {
                    onHideCustomView();
                    return;
                }
                mCustomView = paramView;
                mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                mOriginalOrientation = getRequestedOrientation();
                mCustomViewCallback = paramCustomViewCallback;
                ((FrameLayout)getWindow().getDecorView()).addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846);
            }


            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            final protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext() , "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    //eski---- >   Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            public void onProgressChanged(WebView view, int newProgress){
                progressBar.setProgress(newProgress);
                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setDomStorageEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.getSaveFormData();
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(true);
       // webSettings.setSupportMultipleWindows(true); //?a href problem
        webSettings.getJavaScriptEnabled();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
       // mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
      //  webSettings.setJavaScriptCanOpenWindowsAutomatically(false); //(popup)
    }


    protected void handleSMSLink(String url){
        /*
            If you want to ensure that your intent is handled only by a text messaging app (and not
            other email or social apps), then use the ACTION_SENDTO action
            and include the "smsto:" data scheme
        */

        // Initialize a new intent to send sms message
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        // Extract the phoneNumber from sms url
        String phoneNumber = url.split("[:?]")[1];

        if(!TextUtils.isEmpty(phoneNumber)){
            // Set intent data
            // This ensures only SMS apps respond
            intent.setData(Uri.parse("smsto:" + phoneNumber));

            // Alternate data scheme
            //intent.setData(Uri.parse("sms:" + phoneNumber));
        }else {
            // If the sms link built without phone number
            intent.setData(Uri.parse("smsto:"));

            // Alternate data scheme
            //intent.setData(Uri.parse("sms:" + phoneNumber));
        }


        // Extract the sms body from sms url
        if(url.contains("body=")){
            String smsBody = url.split("body=")[1];

            // Encode the sms body
            try{
                smsBody = URLDecoder.decode(smsBody,"UTF-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

            if(!TextUtils.isEmpty(smsBody)){
                // Set intent body
                intent.putExtra("sms_body",smsBody);
            }
        }

        if(intent.resolveActivity(getPackageManager())!=null){
            // Start the sms app
            startActivity(intent);
        }else {
            Toast.makeText(getApplicationContext(),"No SMS app found.",Toast.LENGTH_SHORT).show();
        }
    }


    // Custom method to handle web view mailto link
    protected void handleMailToLink(String url){
        // Initialize a new intent which action is send
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        // For only email app handle this intent
        intent.setData(Uri.parse("mailto:"));

        String mString="";
        // Extract the email address from mailto url
        String to = url.split("[:?]")[1];
        if(!TextUtils.isEmpty(to)){
            String[] toArray = to.split(";");
            // Put the primary email addresses array into intent
            intent.putExtra(Intent.EXTRA_EMAIL,toArray);
            mString += ("TO : " + to);
        }

        // Extract the cc
        if(url.contains("cc=")){
            String cc = url.split("cc=")[1];
            if(!TextUtils.isEmpty(cc)){
                //cc = cc.split("&")[0];
                cc = cc.split("&")[0];
                String[] ccArray = cc.split(";");
                // Put the cc email addresses array into intent
                intent.putExtra(Intent.EXTRA_CC,ccArray);
                mString += ("\nCC : " + cc );
            }
        }else {
            mString += ("\n" + "No CC");
        }

        // Extract the bcc
        if(url.contains("bcc=")){
            String bcc = url.split("bcc=")[1];
            if(!TextUtils.isEmpty(bcc)){
                //cc = cc.split("&")[0];
                bcc = bcc.split("&")[0];
                String[] bccArray = bcc.split(";");
                // Put the bcc email addresses array into intent
                intent.putExtra(Intent.EXTRA_BCC,bccArray);
                mString += ("\nBCC : " + bcc );
            }
        }else {
            mString+=("\n" + "No BCC");
        }

        // Extract the subject
        if(url.contains("subject=")){
            String subject = url.split("subject=")[1];
            if(!TextUtils.isEmpty(subject)){
                subject = subject.split("&")[0];
                // Encode the subject
                try{
                    subject = URLDecoder.decode(subject,"UTF-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                // Put the mail subject into intent
                intent.putExtra(Intent.EXTRA_SUBJECT,subject);
                mString+=("\nSUBJECT : " + subject );
            }
        }else {
            mString+=("\n" + "No SUBJECT");
        }

        // Extract the body
        if(url.contains("body=")){
            String body = url.split("body=")[1];
            if(!TextUtils.isEmpty(body)){
                body = body.split("&")[0];
                // Encode the body text
                try{
                    body = URLDecoder.decode(body,"UTF-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                // Put the mail body into intent
                intent.putExtra(Intent.EXTRA_TEXT,body);
                mString+=("\nBODY : " + body );
            }
        }else {
            mString+=("\n" + "No BODY");
        }

        // Email address not null or empty
        if(!TextUtils.isEmpty(to)){
            if(intent.resolveActivity(getPackageManager())!=null){
                // Finally, open the mail client activity
                startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(),"No email client found.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    final void oneSignalInit() {
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    123
                            );

                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            123
                    );
                }
            }else {
                // Permission already granted
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(getSupportActionBar().getTitle().equals("Local Page")){
            setTitle("WebView");
            FrameLayout frameLayout = findViewById(R.id.content_frame);
            frameLayout.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            mWebView.loadUrl(url);
        }
        else if(mWebView.canGoBack())
            mWebView.goBack();
        else{
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit the application?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        final public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_back) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
            return true;

        }
        else if(id == R.id.action_refresh){
            mWebView.reload();
        }
        else if(id == R.id.action_share){
            share();
        }
        else if(id == R.id.action_copy){
            copyToPanel(getApplicationContext(),mWebView.getUrl());
            Snackbar snackbar = Snackbar.make(drawerLayout, "Link Copied.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return super.onOptionsItemSelected(item);
    }


    final public void copyToPanel(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied.", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    final void share(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, " Posted By ... : "+mWebView.getUrl());
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homepage_actions, menu);
        getMenuInflater().inflate(R.menu.main, optionsMenu);
        return super.onCreateOptionsMenu(menu);
    }

    final void setFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contact) {
            //contact
        } else if (id == R.id.nav_home) {
            fragment = null;
            setTitle("WebView");
            mWebView.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            mWebView.loadUrl(url);

        } else if (id == R.id.nav_info) {
            frameLayout.setVisibility(View.VISIBLE);
            fragment = new About();
        } else if (id == R.id.nav_share) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                //log.d("TAG", "The intersitial wasn't loaded yet.");
            }
        }

        if (fragment != null) {
           setFragment();
           mWebView.setVisibility(View.GONE);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
