package xyz.fmsoft.studious.Authentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fmsoft.studious.R;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.signup_email)AppCompatEditText _email;
    @BindView(R.id.signup_name)AppCompatEditText _name;
    @BindView(R.id.signup_password)AppCompatEditText _password;
    @BindView(R.id.signup_verify_password)AppCompatEditText _verifyPassword;
    @BindView(R.id.signup_button)AppCompatButton _signup;
    @BindView(R.id.signup_login)AppCompatTextView _login;

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signup_button:

                break;
            case R.id.signup_login:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;
        }
    }
}
