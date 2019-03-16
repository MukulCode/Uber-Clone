package com.example.uberclone;

import android.content.Intent;
import android.print.PrinterId;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.time.temporal.UnsupportedTemporalTypeException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {

        if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {

            if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null && e == null) {

                        Toast.makeText(MainActivity.this, "We have an anonymous user", Toast.LENGTH_SHORT).show();


                        user.put("as", edtDriverOrPassenger.getText().toString());

                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        });
                    }
                }
            });
        }
        } else {
            Toast.makeText(MainActivity.this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
            return;
        }




    }

    enum State {
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            // transition
           // ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }
        btnSignUpLogin = findViewById(R.id.btnSignUpLogin);
        driverRadioButton = findViewById(R.id.rdbDriver);
        passengerRadioButton = findViewById(R.id.rdbPassenger);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        btnOneTimeLogin.setOnClickListener(this);

        state = State.SIGNUP;

        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDOrP);



        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (state == State.SIGNUP) {

                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false) {
                        Toast.makeText(MainActivity.this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRadioButton.isChecked()) {
                        appUser.put("as", "Driver");

                    } else if (passengerRadioButton.isChecked()) {
                        appUser.put("as", "Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                Toast.makeText(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();

                            }
                        }
                    });

                } else if (state == State.LOGIN) {


                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {

                            if (user != null && e == null) {

                                Toast.makeText(MainActivity.this, "User Logged in", Toast.LENGTH_SHORT).show();

                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.loginItem:

                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Log In");
                } else if (state == State.LOGIN) {

                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                }


                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity() {

        if (ParseUser.getCurrentUser() != null) {

            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {

                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }

        }

    }

    private void transitionToDriverRequestListActivity() {

        if (ParseUser.getCurrentUser() != null) {

            if (ParseUser.getCurrentUser().get("as").equals("Driver")) {

                Intent intent = new Intent(this, DriverRequestListActivity.class);
                startActivity(intent);

            }

        }

    }
}
