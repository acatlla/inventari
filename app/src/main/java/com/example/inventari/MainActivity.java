package com.example.inventari;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String PRODUCT = "com.example.inventari.PRODUCT";
    public static final String PRODUCTS_FILENAME = "inventari.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                startActivity(intent);
            }
        });

        displayProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_export) {
            Snackbar.make(getWindow().getDecorView(),"Encara no està implementat", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        } else if (id == R.id.action_clean) {
            cleanProducts();
            return true;
        } else if (id == R.id.action_settings) {
            Snackbar.make(getWindow().getDecorView(),"Encara no està implementat", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayProducts(){
        String[] products = getProducts(PRODUCTS_FILENAME);
        // Display products counter
        TextView productCounter = (TextView) findViewById(R.id.productCounter);
        productCounter.setText(products.length + " productes");
        // Display products list
        final ListView listview = (ListView) findViewById(R.id.productList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, products);
        listview.setAdapter(adapter);
    }

    private void cleanProducts(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Eliminar productes");
        builder.setMessage("S'eliminaran tots els productes. Estas segur d'eliminar-los?");

        builder.setPositiveButton("Acceptar", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(PRODUCTS_FILENAME, Context.MODE_PRIVATE);
                    outputStream.close();
                    Snackbar.make(getWindow().getDecorView(),"S'han eliminat els productes amb èxit", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (Exception e) {
                    Snackbar.make(getWindow().getDecorView(),"No s'han pogut eliminar els productes", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    e.printStackTrace();
                } finally {
                    displayProducts();
                }

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // I do not need any action here you might
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private String[] getProducts(String filename){
        String[] products = {};
        FileInputStream inputStream;
        int character;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            inputStream = openFileInput(filename);
            while((character = inputStream.read()) != -1) {
                stringBuffer.append((char)character);
            }
            inputStream.close();
            if (stringBuffer.length() > 0) {
                products = stringBuffer.toString().split("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

}
