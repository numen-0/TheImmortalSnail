package com.example.theimmortalsnail.activities;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;

import com.example.theimmortalsnail.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends BaseActivity {

    Button loginButton, registerButton, exitButton;

    private static final String SERVER_URL = "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/iduenas003/WEB/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        exitButton = findViewById(R.id.exitButton);

        loginButton.setOnClickListener(v -> showLoginDialog());
        registerButton.setOnClickListener(v -> showRegisterDialog());
        exitButton.setOnClickListener(v -> closeActivity());
    }

    private void showLoginDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_login, null);

        new AlertDialog.Builder(this)
                .setTitle(R.string.login)
                .setView(layout)
                .setPositiveButton(R.string.dialog_log, (dialog, which) -> {
                    EditText nameField = layout.findViewById(R.id.nameField);
                    EditText passwordField = layout.findViewById(R.id.passwordField);

                    String name = nameField.getText().toString();
                    String password = passwordField.getText().toString();

                    if (!name.isEmpty() && !password.isEmpty()) {
                        new LoginTask(this).execute(name, password);
                    } else {
                        Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showRegisterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_register, null);

        new AlertDialog.Builder(this)
                .setTitle(R.string.register)
                .setView(layout)
                .setPositiveButton(R.string.dialog_register, (dialog, which) -> {
                    EditText nameField = layout.findViewById(R.id.nameField);
                    EditText passField = layout.findViewById(R.id.passwordField);
                    EditText confirmField = layout.findViewById(R.id.confirmPasswordField);

                    String name = nameField.getText().toString();
                    String pass = passField.getText().toString();
                    String confirm = confirmField.getText().toString();

                    if (name.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                        Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!pass.equals(confirm)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new RegisterTask(this).execute(name, pass);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    // AsyncTask for login
    private static class LoginTask extends AsyncTask<String, Void, String> {
        LoginActivity activity;
        LoginTask(LoginActivity activity) {
            this.activity = activity;
        }


        @Override
        protected String doInBackground(String... params) {
            try {
                String name = URLEncoder.encode(params[0], "UTF-8");
                String password = URLEncoder.encode(params[1], "UTF-8");
                URL url = new URL(SERVER_URL + "/user.php?name=" + name + "&password=" + password);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                String response = result.toString();  // The raw response
                System.out.println("API Response: " + response);  // Log the response

                return result.toString();
            } catch (Exception e) {
                System.out.println("{\"error\":\"" + e.getMessage() + "\"}");
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject response = new JSONObject(result);
                if (response.has("user")) {
                    JSONObject user = response.getJSONObject("user");
                    int userId = user.getInt("id");
                    // TODO poner esto al singletone y q haga su trabajo
                    Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show();
                    activity.openActivity(MainActivity.class);
                    activity.closeActivity();
                } else if (response.has("error")) {
                    Toast.makeText(activity, "Error: " + response.getString("error"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "Unexpected response", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                System.out.println("Parse error: " + e.getMessage());
                Toast.makeText(activity, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // AsyncTask for registration
    private static class RegisterTask extends AsyncTask<String, Void, String> {
        Context context;
        RegisterTask(Context ctx) { this.context = ctx; }

        @Override
        protected String doInBackground(String... params) {
            try {
                String name = params[0];
                String password = params[1];
                String data = "{\"name\":\"" + name + "\",\"password\":\"" + password + "\"}";

                URL url = new URL(SERVER_URL + "/user.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");

                con.getOutputStream().write(data.getBytes("UTF-8"));

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                return result.toString();
            } catch (Exception e) {
                System.out.println("{\"error\":\"" + e.getMessage() + "\"}");
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            Toast.makeText(context, "Register: " + result, Toast.LENGTH_LONG).show();
        }
    }
}

