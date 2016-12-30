package xyz.fmsoft.studious;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.fmsoft.studious.Authentication.LoginActivity;
import xyz.fmsoft.studious.R;
import xyz.fmsoft.studious.Retrofit.Profile;
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Secret.Environment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreferences;
    private static final String TAG = "MainActivity";
    private Profile profile;

    @BindView(R.id.nav_view)NavigationView navigationView;
    @BindView(R.id.drawer_layout)DrawerLayout drawer;
    @BindView(R.id.toolbar)Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        View headerLayout = navigationView.getHeaderView(0);
        final TextView headerEmail = ButterKnife.findById(headerLayout,R.id.nav_header_email);
        final TextView headerName = ButterKnife.findById(headerLayout, R.id.nav_header_name);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        sharedPreferences = this.getSharedPreferences("token",Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString(getString(R.string.saved_jwt),"");

        if (!token.contains("JWT")) {
            //Token not found direct to Login
            progressDialog.dismiss();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            //Get the profile Here
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

                }
            });

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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
                sharedPreferences.edit().remove(getString(R.string.saved_jwt)).commit();
                startActivity(new Intent(this,MainActivity.class));
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
