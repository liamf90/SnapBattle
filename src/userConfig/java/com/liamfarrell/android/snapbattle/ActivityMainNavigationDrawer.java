package com.liamfarrell.android.snapbattle;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liamfarrell.android.snapbattle.ui.AboutUsActivity;
import com.liamfarrell.android.snapbattle.ui.FollowFacebookFriendsFragment;
import com.liamfarrell.android.snapbattle.ui.BattleChallengesListFragment;
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleCompletedListFragment;
import com.liamfarrell.android.snapbattle.mvvm_ui.BattleCurrentListFragment;
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment;
import com.liamfarrell.android.snapbattle.ui.HomeFeedFragment;
import com.liamfarrell.android.snapbattle.ui.LogoutFragment;
import com.liamfarrell.android.snapbattle.ui.NotificationListActivity;
import com.liamfarrell.android.snapbattle.ui.ProfileFragment;
import com.liamfarrell.android.snapbattle.mvvm_ui.SearchUsersAndBattlesActivity;
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewFollowingFragment;
import com.liamfarrell.android.snapbattle.ui.createbattle.CreateBattleActivity;
import com.liamfarrell.android.snapbattle.ui.createbattle.VerifyBattleFragment;
import com.liamfarrell.android.snapbattle.ui.startup.ChooseProfilePictureStartupFragment;
import com.liamfarrell.android.snapbattle.caches.CurrentUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.notifications.Notification;
import com.liamfarrell.android.snapbattle.caches.NotificationCache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class ActivityMainNavigationDrawer extends AppCompatActivity {

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "https://api.androidhive.info/images/nav-menu-header-bg.jpg";
    private static final String urlProfileImg = "https://lh3.googleusercontent.com/eCtE_G34M9ygdkmOpYvCag1vBARCmZwnVS6rS5t4JLzJ6QgQSBquM0nuTsCpLhYbKljoyS-txg";
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_CHALLENGES = "challenges";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_LOGOUT = "logout";
    private static final String TAG_CURRENT = "current";
    private static final String TAG_COMPLETED = "completed";
    private static final String TAG_ADD_FOLLOWERS = "add_followers";
    private static final String TAG_VIEW_FOLLOWERS = "view_followers";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_SEARCH = "search";
    // index to identify current nav menu item
    public static int navItemIndex = 0;
    public static String CURRENT_TAG = TAG_HOME;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg;
    private CircleImageView imgProfile;
    private TextView txtUsername;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private TextView notificationNumberTextView;
    private MenuItem mNotificationItem;
    private NotificationCache mNotificationCache;
    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private HomeFeedFragment homeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(ChooseProfilePictureStartupFragment.PROFILE_PIC_UPDATED_BROADCAST));

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.content_activity_main_navigation_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        //txtName = (TextView) navHeader.findViewById(R.id.name);
        txtUsername = (TextView) navHeader.findViewById(R.id.usernameTextView);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(ActivityMainNavigationDrawer.this, CreateBattleActivity.class), 200);


            }
        });

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();


        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
    }

    //Broadcast receiver for startup profile pic copied to system
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            updateImageView();
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            updateNotificationsUnseenLayout(mNotificationCache.hasAllNotificationsBeenSeen());
        } catch (NullPointerException e) {
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 200) {
            if (data.hasExtra(VerifyBattleFragment.SNACKBAR_MESSAGE_EXTRA)) {
                showSnackBarNotification(data.getStringExtra(VerifyBattleFragment.SNACKBAR_MESSAGE_EXTRA));
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSnackBarNotification(String message) {
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);


        snackbar.show();
    }


    public void updateImageView() {
        Log.i("ActivityMain", "Loading profile pic  url: " + CurrentUsersProfilePicCacheManager.getProfilePictureSavePath(this));
        Picasso.get().load(new File(CurrentUsersProfilePicCacheManager.getProfilePictureSavePath(this))).placeholder(R.drawable.default_profile_pic100x100).error(R.drawable.default_profile_pic100x100).into(imgProfile, new Callback() {
            @Override
            public void onSuccess() {
                Log.i("ActivityMain", "On success");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

    }


    public void loadUsernameAndName() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = sharedPref.getString(FacebookLoginFragment.NAME_SHAREDPREFS, "");
        String username = sharedPref.getString(FacebookLoginFragment.USERNAME_SHAREDPREFS, "");
        Log.i("MainActivity", "Username: " + username);
        //txtName.setText(name);
        txtUsername.setText(getResources().getString(R.string.atusername, username));

    }


    private void setUpNotificationLoader() {
        mNotificationCache = NotificationCache.getNotificationCache();
        mNotificationCache.LoadListFromFile(this, new NotificationCache.LoadNotificationsCallback() {
            @Override
            public void onNoUpdates(boolean hasAllNotificationsBeenSeen) {
                updateNotificationsUnseenLayout(hasAllNotificationsBeenSeen);
            }

            @Override
            public void onLoaded(LinkedList<Notification> notificationList, boolean hasAllNotificationsBeenSeen) {
                updateNotificationsUnseenLayout(hasAllNotificationsBeenSeen);
            }

            @Override
            public void onUpdates(LinkedList<Notification> notificationUpdatesForTop, boolean hasAllNotificationsBeenSeen) {
                updateNotificationsUnseenLayout(hasAllNotificationsBeenSeen);
            }


        });
        mNotificationCache.setGCMUpdateCallback(new NotificationCache.GCMUpdatesCallback() {
            @Override
            public void onUpdates(boolean hasAllNotificationsBeenSeen) {
                updateNotificationsUnseenLayout(hasAllNotificationsBeenSeen);
            }

        });
    }

    private void updateNotificationsUnseenLayout(final boolean hasAllNotificationsBeenSeen) {
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                if (hasAllNotificationsBeenSeen) {
                    notificationNumberTextView.setVisibility(View.GONE);
                } else {
                    notificationNumberTextView.setVisibility(View.VISIBLE);
                }
            }
        });


    }


    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
        loadUsernameAndName();


        Picasso.get().load(R.drawable.navbackground2).into(imgNavHeaderBg);
        // Loading profile image

        CurrentUsersProfilePicCacheManager profilePicManger = new CurrentUsersProfilePicCacheManager(this);
        profilePicManger.getProfilePicSaved(imgProfile);
        profilePicManger.checkForProfilePicUpdate(imgProfile);


    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    public void showFab() {
        fab.show();
    }

    public void hideFab() {
        fab.hide();
    }

    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                if (homeFragment == null) {
                    homeFragment = new HomeFeedFragment();
                }
                return homeFragment;
            case 1:
                BattleCurrentListFragment currentBattlesFragment = new BattleCurrentListFragment();
                return currentBattlesFragment;
            case 2:
                // current fragment
                BattleCurrentListFragment currentBattlesFragment2 = new BattleCurrentListFragment();
                return currentBattlesFragment2;
            case 3:
                // completed fragment
                BattleCompletedListFragment battleCompletedListFragment = new BattleCompletedListFragment();
                return battleCompletedListFragment;
            case 4:
                // challenges
                BattleChallengesListFragment challengesFragment = new BattleChallengesListFragment();
                return challengesFragment;
            case 5:
                // profile
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case 6:
                // current fragment
                FollowFacebookFriendsFragment followFacebookFriendsFragment = new FollowFacebookFriendsFragment();
                return followFacebookFriendsFragment;
            case 7:
                // current fragment
                ViewFollowingFragment viewFollowingFragment = new ViewFollowingFragment();
                return viewFollowingFragment;
            case 8:
                // Logout fragment
                LogoutFragment logoutFragment = new LogoutFragment();
                return logoutFragment;


            default:
                return new HomeFeedFragment();
        }
    }


    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_create_battle:
                        // launch new intent instead of loading fragment
                        startActivityForResult(new Intent(ActivityMainNavigationDrawer.this, CreateBattleActivity.class), 200);
                        break;
                    case R.id.nav_current_battles:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_CURRENT;
                        break;
                    case R.id.nav_completed_battles:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_COMPLETED;
                        break;
                    case R.id.nav_challenges:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_CHALLENGES;
                        break;
                    case R.id.nav_profile:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_PROFILE;
                        break;
                    case R.id.nav_add_followers:
                        navItemIndex = 6;
                        CURRENT_TAG = TAG_ADD_FOLLOWERS;
                        break;
                    case R.id.nav_view_followers:
                        navItemIndex = 7;
                        CURRENT_TAG = TAG_VIEW_FOLLOWERS;
                        break;
                    case R.id.nav_logout:
                        navItemIndex = 8;
                        CURRENT_TAG = TAG_LOGOUT;
                        break;


                    case R.id.nav_about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ActivityMainNavigationDrawer.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;

                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
            mNotificationItem = menu.findItem(R.id.notificationBell);


            mNotificationItem.setActionView(R.layout.notification_layout);
            notificationNumberTextView = mNotificationItem.getActionView().findViewById(R.id.badge_notification_1);


            mNotificationItem.getActionView().findViewById(R.id.button1).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i("ActivityMain", "Notification Icon clicked");
                            Intent intent = new Intent(ActivityMainNavigationDrawer.this, NotificationListActivity.class);
                            startActivity(intent);
                        }
                    });

            setUpNotificationLoader();


            // showing dot next to notifications label


            //menu.getItem(0).setActionView(R.layout.menu_dot);

        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.notificationBell) {
            Log.i("ActivityMain", "Notification Bell Clicked");
            return true;
        } else if (id == R.id.action_search) {

            Intent intent = new Intent(this, SearchUsersAndBattlesActivity.class);
            startActivity(intent);
            //navItemIndex = 10;
            // CURRENT_TAG = TAG_SEARCH;
            //loadHomeFragment();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    // show or hide the fab
    private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
    }


}