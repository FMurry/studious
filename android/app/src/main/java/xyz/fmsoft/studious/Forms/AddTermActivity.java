package xyz.fmsoft.studious.Forms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.fmsoft.studious.Authentication.LoginActivity;
import xyz.fmsoft.studious.R;
import xyz.fmsoft.studious.Retrofit.Response;
import xyz.fmsoft.studious.Retrofit.RetrofitInterface;
import xyz.fmsoft.studious.Secret.Environment;
import xyz.fmsoft.studious.Utils.DatePickerFragment;

public class AddTermActivity extends AppCompatActivity implements View.OnClickListener, DatePickerFragment.DateListener{


    private static final String TAG = "AddTermActivity";

    @BindView(R.id.addterm_name)AppCompatEditText _name;
    @BindView(R.id.addterm_school)AppCompatEditText _school;
    @BindView(R.id.addterm_startdate)AppCompatButton _startDate;
    @BindView(R.id.addterm_enddate)AppCompatButton _endDate;
    @BindView(R.id.addterm_submit)AppCompatButton _submit;
    @BindView(R.id.addterm_typespinner)AppCompatSpinner _typeSpinner;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_term);
        ButterKnife.bind(this);
        ArrayList<String> list = new ArrayList<>();
        list.add("Choose Term Type Here");
        list.add("Semester");
        list.add("Trimester");
        list.add("Quarter");
        ArrayAdapter<String> types = new ArrayAdapter<>(this,R.layout.app_spinner, list);
        _typeSpinner.setAdapter(types);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.add_term_title));
        }
        SharedPreferences sharedPreferences = this.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.saved_jwt),"");

        if (!token.contains("JWT")) {
            //Token Not Valid Open Login and clear other Activities
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        _startDate.setOnClickListener(this);
        _endDate.setOnClickListener(this);
        _submit.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED);
        finish();
        return super.onOptionsItemSelected(item);
    }

    public boolean isValid(){
        boolean valid = true;
        String name = _name.getText().toString();
        String school = _school.getText().toString();
        String startDate = _startDate.getText().toString();
        String endDate = _endDate.getText().toString();

        //Name Validation
        if(name.isEmpty() || name.trim().length() <= 1){
            _name.setError("Name must be greater than one character");
            valid = false;
        }
        else if(name.trim().length() >= 40) {
            _name.setError("Name must be less than 40 characters");
            valid = false;
        }
        else{
            _name.setError(null);
        }

        //School Validation
        if(school.trim().isEmpty() || school.trim().length() <= 1){
            _school.setError("School must be greater than one character");
            valid = false;
        }
        else if(school.trim().length() >= 60) {
            _school.setError("Name must be less than 60 characters");
            valid = false;
        }
        else{
            _school.setError(null);
        }

        //Start Date Validation
        if(startDate.trim().isEmpty() || startDate.trim().toLowerCase().equals("start date")) {
            _startDate.setError("Please set a start date");
            valid = false;
        }
        else {
            _startDate.setError(null);
        }

        //End Date Validation
        if(endDate.isEmpty() || endDate.toLowerCase().equals("end date")) {
            _endDate.setError("Please set a end date");
            valid = false;
        }
        else {
            _endDate.setError(null);
        }

        //Type Validation
        if((_typeSpinner.getSelectedItem()).equals("Choose Term Type Here")){
            valid = false;
        }
        else{

        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addterm_startdate:
                showDatePicker(view,1);
                break;
            case R.id.addterm_enddate:
                showDatePicker(view,2);
                break;
            case R.id.addterm_submit:
                if(isValid()){
                    final String name = _name.getText().toString();
                    final String school = _school.getText().toString().trim();
                    final String startDate = _startDate.getText().toString().trim();
                    final String endDate = _endDate.getText().toString().trim();
                    final String type = _typeSpinner.getSelectedItem().toString().trim();
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Please Wait");
                    progressDialog.show();
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
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
                            Call<Response> call = request.addTerm(token,name,school,startDate,endDate,type);
                            call.enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Response r = response.body();
                                    if(r.getCode() == 200) {
                                        //Successful
                                        setResult(RESULT_OK);
                                        progressDialog.dismiss();
                                        finish();
                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Log.d(TAG, r.getMsg());
                                    }
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Log.d(TAG,t.getMessage());
                                    Log.d(TAG,"Internal Server Error");
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    },3000);

                }
                break;
        }
    }

    public void showDatePicker(View v, int type){
        DialogFragment dialogFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("key",type);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(),"datePicker");
    }

    @Override
    public void returnFormattedDate(String date, int key) {
        if(key == 1){
            _startDate.setText(date);
        }
        else{
            _endDate.setText(date);
        }
    }
}
