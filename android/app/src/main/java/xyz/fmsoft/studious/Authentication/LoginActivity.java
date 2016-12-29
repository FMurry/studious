package xyz.fmsoft.studious.Authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.login_email)AppCompatEditText _email;
    @BindView(R.id.login_password)AppCompatEditText _password;
    @BindView(R.id.login_button)AppCompatButton _loginButton;

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


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button:
                //TODO: Add ProgressDialog
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Please Wait.....");
                progressDialog.setCancelable(false);
                progressDialog.show();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Environment.apiUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitInterface request = retrofit.create(RetrofitInterface.class);
                Call<Login> call = request.login(_email.getText().toString(),_password.getText().toString());
                call.enqueue(new Callback<Login>() {
                    @Override
                    public void onResponse(Call<Login> call, Response<Login> response) {
                        Login login = response.body();
                        Log.d(TAG,"Success: "+login.getSuccess());
                        Log.d(TAG,"Code: "+login.getCode());
                        Log.d(TAG,"token: "+login.getToken());
                        if (login.getCode() == 200){
                            SharedPreferences sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.putString(getString(R.string.saved_jwt),login.getToken());
                            editor.commit();
                            progressDialog.dismiss();
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            finish();
                        }
                        else if(login.getCode() == 502){
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Password Incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Login> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });
                break;

        }
    }
}
