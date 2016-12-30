package xyz.fmsoft.studious.Authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

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
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Retrofit.Signup;
import xyz.fmsoft.studious.Secret.Environment;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.signup_email)AppCompatEditText _email;
    @BindView(R.id.signup_name)AppCompatEditText _name;
    @BindView(R.id.signup_password)AppCompatEditText _password;
    @BindView(R.id.signup_verify_password)AppCompatEditText _verifyPassword;
    @BindView(R.id.signup_button)AppCompatButton _signup;
    @BindView(R.id.signup_login)AppCompatTextView _login;

    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        _signup.setOnClickListener(this);
        _login.setOnClickListener(this);
    }

    public boolean isSignupValid() {
        boolean valid = true;
        String email = _email.getText().toString();
        String name = _name.getText().toString();
        String password = _password.getText().toString();
        String verifyPassword = _verifyPassword.getText().toString();

        //Email Verification
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            _email.setError("Enter a valid Email");
            valid = false;
        }
        else{
            _email.setError(null);
        }

        //Password Verification
        if(password.length() < 8 && password.length() > 24){
            _password.setError("Password between 8 and 24 Alphanumeric characters");
            valid = false;
        }
        else{
            _password.setError(null);
        }
        //Verify password verification
        if(!password.equals(verifyPassword)){
            if(_password.getError() == null) {
                _password.setError("Passwords don't match");
            }
            _verifyPassword.setError("Passwords don't match");
            valid = false;
        }
        else {
            _password.setError(null);
            _verifyPassword.setError(null);
        }

        if(name.isEmpty() ||  (name.length() < 3 && name.length() > 48)) {
            _name.setError("Name should be between 3 and 48 characters");
            valid = false;
        }
        else{
            _name.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signup_button:
                if(isSignupValid()) {
                    final String email = _email.getText().toString();
                    final String password = _password.getText().toString();
                    String name = _name.getText().toString();
                    final ProgressDialog progressDialog = new ProgressDialog(this);
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
                    final RetrofitInterface request = retrofit.create(RetrofitInterface.class);
                    Call<Signup> call = request.signup(email, password, name);
                    call.enqueue(new Callback<Signup>() {
                        @Override
                        public void onResponse(Call<Signup> call, Response<Signup> response) {
                            Signup signup = response.body();
                            if(signup.getCode() == 200){
                                //Signup worked
                                Call<Login> newCall = request.login(email,password);
                                newCall.enqueue(new Callback<Login>() {
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
                                        }
                                        else {
                                            progressDialog.dismiss();
                                            Log.d(TAG, "Code: "+login.getCode());
                                            startActivity(new Intent(getBaseContext(), LoginActivity.class));
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Login> call, Throwable t) {
                                        progressDialog.dismiss();
                                        Log.d(TAG, t.getMessage());
                                        startActivity(new Intent(getBaseContext(), LoginActivity.class));
                                        finish();

                                    }
                                });
                            }
                            else if(signup.getCode() == 401) {
                                //User already has account
                                //Redirect to login
                                Toast.makeText(SignupActivity.this, "Email already associated with Account", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                                finish();
                                progressDialog.dismiss();
                            }

                        }

                        @Override
                        public void onFailure(Call<Signup> call, Throwable t) {
                            Log.d(TAG,t.getMessage());
                            progressDialog.dismiss();
                            Toast.makeText(SignupActivity.this, "Internal Server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.signup_login:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;
        }
    }
}
