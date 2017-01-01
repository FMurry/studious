package xyz.fmsoft.studious.Authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.fmsoft.studious.MainActivity;
import xyz.fmsoft.studious.R;
import xyz.fmsoft.studious.Retrofit.Login;
import xyz.fmsoft.studious.Retrofit.Profile;
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Secret.Environment;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    @BindView(R.id.login_email)AppCompatEditText _email;
    @BindView(R.id.login_password)AppCompatEditText _password;
    @BindView(R.id.login_button)AppCompatButton _loginButton;
    @BindView(R.id.login_signup)AppCompatTextView _signup;
    @BindView(R.id.login_error)AppCompatTextView _error;
    @BindView(R.id.login_google_button)SignInButton _googleLoginButton;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 10002;

    private String googleEmail;
    private String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
    private String SCOPES = "oauth2:" + Scopes.PLUS_LOGIN+" https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
    private String oldToken="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Log.d(TAG,SCOPES);
        _googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        _loginButton.setOnClickListener(this);
        _signup.setOnClickListener(this);
        _googleLoginButton.setOnClickListener(this);
        


    }

    public boolean isLoginValid() {
        boolean valid = true;
        String email = _email.getText().toString();
        String password = _password.getText().toString();
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            _email.setError("Enter a valid Email");
            valid = false;
        }
        else{
            _email.setError(null);
        }

        if(password.length() < 8 && password.length() > 24){
            _email.setError("Password between 8 and 24 Alphanumeric characters");
            valid = false;
        }
        else{
            _password.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button:
                if(isLoginValid()) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    _error.setVisibility(View.GONE);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Please Wait.....");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
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
                    Call<Login> call = request.login(_email.getText().toString(), _password.getText().toString());
                    call.enqueue(new Callback<Login>() {
                        @Override
                        public void onResponse(Call<Login> call, Response<Login> response) {
                            Login login = response.body();
                            if (login.getCode() == 200) {
                                SharedPreferences sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.putString(getString(R.string.saved_jwt), login.getToken());
                                editor.commit();
                                progressDialog.dismiss();
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                finish();
                            } else if (login.getCode() == 502) {
                                //Password Incorrect
                                progressDialog.dismiss();
                                _error.setText(getResources().getString(R.string.login_incorrect));
                                _error.setVisibility(View.VISIBLE);
                                _loginButton.setEnabled(false);
                                _password.addTextChangedListener(LoginActivity.this);
                            } else {
                                progressDialog.dismiss();
                                if (login.getCode() == 501) {
                                    //No User found
                                    _error.setText(getResources().getString(R.string.login_incorrect));
                                    _error.setVisibility(View.VISIBLE);
                                    _loginButton.setEnabled(false);
                                    _password.addTextChangedListener(LoginActivity.this);
                                }
                                Log.d(TAG, login.getSuccess());
                            }
                        }

                        @Override
                        public void onFailure(Call<Login> call, Throwable t) {
                            Log.d(TAG, t.getMessage());
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.login_signup:
                startActivity(new Intent(this, SignupActivity.class));
                finish();
                break;
            case R.id.login_google_button:
                pickUserAccount();
                //Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                googleEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.d(TAG,googleEmail);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, "Please Choose A google Account", Toast.LENGTH_SHORT).show();
            }
        }
        else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getUsername();
        }
    }

    public void googleLogin(String token) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        _error.setVisibility(View.GONE);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
                Profile profile = response.body();
                Log.d(TAG,profile.toString());
                progressDialog.dismiss();
                if(profile.getCode()==200){
                    Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Internal Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        _loginButton.setEnabled(true);
        _password.removeTextChangedListener(this);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        if (googleEmail == null) {
            pickUserAccount();
        } else {
//            if (isDeviceOnline()) {
//                new GetUsernameTask(LoginActivity.this, googleEmail, SCOPE).execute();
//            } else {
//                Toast.makeText(this, "Device is offline", Toast.LENGTH_LONG).show();
//            }
            new GetUsernameTask(LoginActivity.this, googleEmail, SCOPES).execute();
        }
    }

    public class GetUsernameTask extends AsyncTask<Void, Void, Void> {
        LoginActivity mActivity;
        String mScope;
        String mEmail;
        String authToken;

        GetUsernameTask(LoginActivity activity, String name, String scope) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = name;
            authToken="";
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String token = fetchToken();
                if (token != null) {
                    Log.d(TAG, "Got the token");
                    Log.d(TAG, token);
                    // **Insert the good stuff here.**
                    // Use the token to access the user's Google data.
                    authToken=token;


                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                Log.d(TAG,e.getMessage());

            }
            return null;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                GoogleAuthUtil.clearToken(mActivity,mActivity.oldToken);
                oldToken = GoogleAuthUtil.getToken(mActivity,mEmail,mScope);
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                mActivity.handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                Log.d(TAG,fatalException.getMessage());

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mActivity.googleLogin(authToken);
        }
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
                            LoginActivity.this,
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
