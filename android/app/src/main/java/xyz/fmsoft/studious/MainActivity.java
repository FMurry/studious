package xyz.fmsoft.studious;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.fmsoft.studious.Authentication.GetUsernameTask;
import xyz.fmsoft.studious.Authentication.LoginActivity;
import xyz.fmsoft.studious.Forms.AddTermActivity;
import xyz.fmsoft.studious.R;
import xyz.fmsoft.studious.Retrofit.Profile;
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Retrofit.Term;
import xyz.fmsoft.studious.Secret.Environment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private SharedPreferences sharedPreferences;
    private static final String TAG = "MainActivity";
    private static final int ADD_TERM_FLAG = 1;
    private static final int ADD_COURSE_FLAG = 2;
    private static final int ADD_ASSIGNMENT_FLAG = 3;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 10002;

    private Profile profile;

    @BindView(R.id.nav_view)NavigationView navigationView;
    @BindView(R.id.drawer_layout)DrawerLayout drawer;
    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(R.id.main_fab_menu)FloatingActionMenu fabMenu;
    @BindView(R.id.main_fab_addCourse)FloatingActionButton addCourseButton;
    @BindView(R.id.main_fab_addAssignment)FloatingActionButton addAssignmentButton;
    @BindView(R.id.main_fab_addTerm)FloatingActionButton addTermButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        addCourseButton.setOnClickListener(this);
        addAssignmentButton.setOnClickListener(this);
        addTermButton.setOnClickListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        final TextView headerEmail = ButterKnife.findById(headerLayout,R.id.nav_header_email);
        final TextView headerName = ButterKnife.findById(headerLayout, R.id.nav_header_name);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        //progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        sharedPreferences = this.getSharedPreferences("token",Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString(getString(R.string.saved_jwt),"");
        final String googleID = sharedPreferences.getString(getString(R.string.saved_googleID),"");
        final String googleEmail = sharedPreferences.getString(getString(R.string.saved_googleEmail),"");
        if (token.equals("") && googleID.equals("") && googleEmail.equals("")) {
            //Token not found direct to Login
            progressDialog.dismiss();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else if(token.contains("JWT")){
            //User Logged in with email and password
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Environment.apiUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            RetrofitInterface request = retrofit.create(RetrofitInterface.class);
            Call<Profile> call = request.getProfile(token);
            call.enqueue(new Callback<Profile>() {
                @Override
                public void onResponse(Call<Profile> call, Response<Profile> response) {
                    profile = response.body();
                    if(profile == null){
                        //Login Failed Somehow
                        //Remove the token and have user sign in again
                        sharedPreferences.edit().remove(getString(R.string.saved_jwt)).commit();
                        progressDialog.dismiss();
                        startActivity(new Intent(getBaseContext(), LoginActivity.class));
                        finish();
                    }
                    else if(profile.getCode() == 200) {
                        //Profile Retrieved successfully
                        Log.d(TAG,profile.toString());
                        if(profile.getUser().isVerified()) {
                            headerEmail.setText(profile.getUser().getEmail());
                        }
                        else{
                            headerEmail.setText(profile.getUser().getEmail()+" (unverified)");
                        }
                        invalidateOptionsMenu();
                        headerName.setText(profile.getUser().getName());
                        progressDialog.dismiss();
                    }
                    else{
                        //Login Failed Somehow
                        //Remove the token and have user sign in again
                        sharedPreferences.edit().remove(getString(R.string.saved_jwt)).commit();
                        progressDialog.dismiss();
                        startActivity(new Intent(getBaseContext(), LoginActivity.class));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Profile> call, Throwable t) {
                    Log.d(TAG,t.getMessage());
                    Toast.makeText(getBaseContext(),"Internal Server Error",Toast.LENGTH_LONG);
                }
            });

        }
        else if((!googleEmail.equals("")) && (!googleID.equals(""))){
            //User Logged in with google Account
            final String SCOPES = "oauth2:" + Scopes.PLUS_LOGIN+" https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
            new GetUsernameTask(MainActivity.this, googleEmail, SCOPES).execute();
            progressDialog.dismiss();
        }
        else{
            //User Not Logged In
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.saved_googleEmail));
            editor.remove(getString(R.string.saved_jwt));
            editor.remove(getString(R.string.saved_googleID));
            editor.commit();
            startActivity(new Intent(this,LoginActivity.class));
            finish();

        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        ArrayList<String> terms = new ArrayList<>();
        if(profile != null && profile.getUser() != null) {
            for (Term term : profile.getUser().getTerms()) {
                terms.add(term.getName());
            }
        }
        else{

        }

        if(terms.size() > 0) {
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem menuItem = menu.findItem(R.id.action_spinner);
            Spinner spinner = (Spinner) MenuItemCompat.getActionView(menuItem);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.app_bar_spinner, terms);
            spinner.setAdapter(adapter);
        }
        else{
            getMenuInflater().inflate(R.menu.default_main, menu);
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
        switch (id){
            case R.id.action_spinner:

                break;
            case R.id.action_add_term:
                Log.d(TAG, "Add Term Pressed");
                Intent addTerm = new Intent(this, AddTermActivity.class);
                startActivityForResult(addTerm, ADD_TERM_FLAG);
                break;
        }
        if (id == R.id.action_spinner) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_course:

                break;
            case R.id.nav_assignment:

                break;
            case R.id.nav_account:

                break;
            case R.id.nav_logout:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(getString(R.string.saved_googleEmail));
                editor.remove(getString(R.string.saved_jwt));
                editor.remove(getString(R.string.saved_googleID));
                editor.commit();
                startActivity(new Intent(this,MainActivity.class));
                finish();
                break;
            case R.id.nav_send:
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_contact)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent, "Send Email using"));
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_fab_addCourse:
                //TODO: Add Course Here
                fabMenu.close(false);
                break;
            case R.id.main_fab_addAssignment:
                //TODO: Add assignment here
                fabMenu.close(true);
                break;
            case R.id.main_fab_addTerm:
                Log.d(TAG, "Add Term Pressed");
                Intent addTerm = new Intent(this, AddTermActivity.class);
                startActivityForResult(addTerm, ADD_TERM_FLAG);
                fabMenu.close(false);
                break;
        }

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_COURSE_FLAG:
                if(resultCode == RESULT_OK) {
                    //User Created a Course
                    startActivity(getIntent());
                }
                else{
                    //User Cancelled
                    Log.d(TAG,"Add Course Cancelled");
                }
                break;
            case ADD_ASSIGNMENT_FLAG:
                if(resultCode == RESULT_OK) {
                    //User Created an Assignment
                    startActivity(getIntent());
                }
                else{
                    //User Cancelled
                    Log.d(TAG,"Add Assignment Cancelled");
                }
                break;
            case ADD_TERM_FLAG:
                if(resultCode == RESULT_OK) {
                    //User created a Term
                    finish();
                    startActivity(getIntent());

                    Log.d(TAG,"Recieved reset from Add Term");
                }
                else{
                    //User Cancelled
                    Log.d(TAG,"Add Course Cancelled");
                }
                break;
        }
    }


    public void googleLogin(String token) {
        View headerLayout = navigationView.getHeaderView(0);
        final TextView headerEmail = ButterKnife.findById(headerLayout,R.id.nav_header_email);
        final TextView headerName = ButterKnife.findById(headerLayout, R.id.nav_header_name);
        final CircleImageView profileImage = ButterKnife.findById(headerLayout, R.id.nav_header_profile_image);
        final ImageView gplusIcon = ButterKnife.findById(headerLayout, R.id.nav_header_gplus);
        gplusIcon.setVisibility(View.VISIBLE);
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Environment.apiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        RetrofitInterface request = retrofit.create(RetrofitInterface.class);
        Call<Profile> call = request.googleLogin(token);
        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                profile = response.body();
                if(profile == null){
                    //Login Failed Somehow
                    //Remove the token and have user sign in again
                    sharedPreferences.edit().remove(getString(R.string.saved_jwt)).commit();
                    startActivity(new Intent(getBaseContext(), LoginActivity.class));
                    finish();
                }
                else if(profile.getCode() == 200) {
                    //Profile Retrieved successfully
                    Log.d(TAG,profile.toString());
                    if(profile.getUser().isVerified()) {
                        headerEmail.setText(profile.getUser().getEmail());
                    }
                    else{
                        headerEmail.setText(profile.getUser().getEmail()+" (unverified)");
                    }
                    invalidateOptionsMenu();
                    headerName.setText(profile.getUser().getName());
                    Picasso.with(getBaseContext())
                            .load(profile.getUser().getImageURL())
                            .fit()
                            .into(profileImage);
                }
                else{
                    //Login Failed Somehow
                    //Remove the token and have user sign in again
                    sharedPreferences.edit().remove(getString(R.string.saved_jwt)).commit();
                    startActivity(new Intent(getBaseContext(), LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast.makeText(MainActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }
}
