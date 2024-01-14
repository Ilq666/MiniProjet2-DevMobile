package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    Spinner spinner1;
    Spinner spinner2;
    EditText amount1;
    EditText newAmou;
    Button cvtButton;
    private Map<String, Double> currencyNumberMap; // Map to store currency codes and their numbers
    private static final int MAX_VISIBLE_ITEMS = 5;
    private ArrayAdapter<String> adapter;
    private JsonObject ratesObject;
    private String[] currencyCodes;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Spinners
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        amount1 = findViewById(R.id.Amount);
        newAmou = findViewById(R.id.newAmount);
        cvtButton = findViewById(R.id.convertir);
        // Initialize the map
        currencyNumberMap = new HashMap<>();
        // Execute AsyncTask to perform network request
        new GetDataTask().execute();
        // Set a listener for item selection in spinner1
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected currency code from spinner1
                String selectedCurrencyCode = (String) parentView.getItemAtPosition(position);
                // Get the corresponding number for the selected currency code
                double number = getNumberForCurrencyCode(selectedCurrencyCode);
                // Store the selected currency code and number in the map
                currencyNumberMap.put("spinner1", number);
                // Do something with the number if needed
//                Log.i("TAG1", "Spinner1 Selected: " + selectedCurrencyCode);
                Log.i("numberr", "Spinner1 Selected number rate: " + currencyNumberMap.get("spinner1").toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Set a listener for item selection in spinner2
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected currency code from spinner2
                String selectedCurrencyCode = (String) parentView.getItemAtPosition(position);
                // Get the corresponding number for the selected currency code
                double number = getNumberForCurrencyCode(selectedCurrencyCode);
                // Do something with the number if needed
                currencyNumberMap.put("spinner2", number);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Set a click listener for the "Convertir" button
        cvtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the amount entered in amountEditText1
                String amountString = amount1.getText().toString();

                if (!amountString.isEmpty()) {
                    double amount = Double.parseDouble(amountString);
                    Log.i("TAGnnnn", "Number Spinner 1: " + amount);
                    // Perform the conversion only if ratesObject is not null
                    if (ratesObject != null ) {
                        double numberSpinner1 = currencyNumberMap.get("spinner1");
                        double numberSpinner2 = currencyNumberMap.get("spinner2");


                        if (!Double.isNaN(numberSpinner1) && !Double.isNaN(numberSpinner2)) {
                            double calculatedValue = amount * (numberSpinner2 / numberSpinner1);

                            amount1.setText(String.valueOf(amount));
                            // After calculation, set the result to newAmountEditText
                            newAmou.setText(String.valueOf(formatDecimal(calculatedValue)));
                        } else {
                            Log.i("TAGuuuii", "Invalid conversion rates. Check your map and rates extraction.");
                        }
                    } else {
                        Log.i("TAGggguuu", "Rates object is null or missing. Data might not be fetched yet.");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper method to format a decimal number to display only 4 digits after the decimal point
    private String formatDecimal(double value) {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.####");
        return decimalFormat.format(value);
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Call the method to get data and parse currency codes
                return getData();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseData) {
            if (responseData != null) {
                JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
                ratesObject = jsonObject.getAsJsonObject("rates");
                currencyNumberMap = extractCurrencyCodesAndNumbers(ratesObject);
                Log.i("TAGrates", "Number Sper 1: " + ratesObject);
                Log.i("TAGnbmap", "Number Sp: " + currencyNumberMap);
                TreeMap<String, Double> sortedMap = new TreeMap<>(currencyNumberMap);
                Log.i("TAGnbmap", "Number Sp: " + sortedMap);

                // Create ArrayAdapter and set it to the Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>(sortedMap.keySet()));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner1.setAdapter(adapter);
                spinner2.setAdapter(adapter);

                // Set the number of visible items
                spinner1.setDropDownVerticalOffset(MAX_VISIBLE_ITEMS * (int) getResources().getDisplayMetrics().density);
                // Set the number of visible items
                spinner2.setDropDownVerticalOffset(MAX_VISIBLE_ITEMS * (int) getResources().getDisplayMetrics().density);

                Log.i("TAG4444", "Rates: " + ratesObject.toString());


            } else {
                // Handle the case where there was an error fetching data
                Log.i("TAG0101010101", "Error fetching data");
            }
        }
    }

    public String getData() throws IOException {
        String apiKey = "0da75b95fbdd8f0028cb79a0b24301ea";
        String apiUrl = "http://data.fixer.io/api/latest?access_key=" + apiKey;
        String responseData = "";
        Map<String, Object> resultMap = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Parse the JSON response
                responseData = response.toString();
                Log.i("TAG11111", "Response Data: " + responseData);

                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
                resultMap = gson.fromJson(responseData, type);
                HashMap<String, String> hmData = new HashMap<String, String>();
                resultMap.forEach((key, value) -> hmData.put(key, String.valueOf(value)));

                // ... (remaining code unchanged)
            } else {
                Log.i("TAGs", "Failed to retrieve data. HTTP Error Code: " + responseCode);
            }
        } catch (IOException e) {
            Log.i("TAG143143", "Exception while fetching data: " + e.getMessage());
        }

        return responseData;
    }


    private Map<String, Double> extractCurrencyCodesAndNumbers(JsonObject ratesObject) {
        Map<String, Double> currencyNumberMap = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entrySet = ratesObject.entrySet();

        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String currencyCode = entry.getKey();
            double number = entry.getValue().getAsDouble();
            currencyNumberMap.put(currencyCode, number);
        }

        return currencyNumberMap;
    }

    private double getNumberForCurrencyCode(String currencyCode) {
        // Use the currencyNumberMap directly to get the number for the given currency code
        if (currencyNumberMap != null && currencyNumberMap.containsKey(currencyCode)) {
            return currencyNumberMap.get(currencyCode);
        } else {
            Log.i("TAGnumber", "Number not found for currency code: " + currencyCode);
            return 0.0; // Default value or handle the case as needed
        }
    }
}
