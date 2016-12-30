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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Secret.Environment;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    @BindView(R.id.login_email)AppCompatEditText _email;
    @BindView(R.id.login_password)AppCompatEditText _password;
    @BindView(R.id.login_button)AppCompatButton _loginButton;
    @BindView(R.id.login_signup)AppCompatTextView _signup;
    @BindView(R.id.login_error)AppCompatTextView _error;

    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        _loginButton.setOnClickListener(this);
        _signup.setOnClickListener(this);


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

        }
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
}
