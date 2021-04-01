package com.example.solvro_application;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.character.CharacterModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private List<CharacterModel> characters=new ArrayList<>();
    private final HashMap<String, String> episodeMap = new HashMap<>();
    private boolean isFavorites;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        isFavorites=i.getBooleanExtra("IsFavorites", false);
        if(!isFavorites) {
            fetchCharacters();
        } else{
            fetchFavorites();
        }
        configureBottomBar();
        configureSpinner();
        findViewById(R.id.listofcharacters).setVisibility(View.INVISIBLE);
    }
    private void configureSpinner(){
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void configureBottomBar(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation1);
        if(isFavorites) bottomNavigationView.setSelectedItemId(R.id.action_favorites);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_home){
                Intent i= new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("IsFavorites", false);
                startActivity(i);
                return true;
            }
            else if(id == R.id.action_favorites){
                Intent i= new Intent(MainActivity.this, MainActivity.class);
                i.putExtra("IsFavorites", true);
                startActivity(i);

                return true;
            }

            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        if(!isFavorites) {
            fetchCharacters();
        } else{
            fetchFavorites();
        }
        String[] charactersArray = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            charactersArray = filteredCharacters();
        }
        ListView listView = findViewById(R.id.listofcharacters);
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, charactersArray);
        listView.setAdapter(adapter);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private JsonObjectRequest addEpisodes(String episodeUrl){
        return new JsonObjectRequest
                (Request.Method.GET, episodeUrl, null, response -> {
                    try {
                        JSONArray results=response.getJSONArray("results");
                        for(int i=0; i<results.length(); i++){
                            episodeMap.put(String.valueOf(results.getJSONObject(i).getInt("id")), results.getJSONObject(i).getString("name"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("error!---> ", error.networkResponse.toString()));
    }

    public JsonObjectRequest addCharacters(String url){
        return new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        JSONArray results=response.getJSONArray("results");
                        for(int i=0; i<results.length(); i++){
                            ArrayList<String> episodes = new ArrayList<>();
                            for(int j=0; j<results.getJSONObject(i).getJSONArray("episode").length();j++){
                                String[] temp=results.getJSONObject(i).getJSONArray("episode").getString(j).split("/");
                                episodes.add(episodeMap.get(temp[temp.length-1]));
                            }
                            characters.add(new CharacterModel(results.getJSONObject(i).getString("name"),
                                    results.getJSONObject(i).getString("status"),
                                    results.getJSONObject(i).getString("url"),
                                    episodes,
                                    results.getJSONObject(i).getJSONObject("location").getString("name"),
                                    results.getJSONObject(i).getString("species"),
                                    results.getJSONObject(i).getString("gender"),
                                    results.getJSONObject(i).getJSONObject("origin").getString("name"),
                                    results.getJSONObject(i).getString("image")
                            ));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("error!---> ", error.networkResponse.toString()));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fetchCharacters(){
        if(isNetworkAvailable()){
            RequestQueue queue = Volley.newRequestQueue(this);
            for(int j=1;j<4;j++){
                queue.add(addEpisodes("https://rickandmortyapi.com/api/episode?page="+j));
            }

            for(int i=1;i<35;i++){
                queue.add(addCharacters("https://rickandmortyapi.com/api/character?page="+i));
            }
        } else {
            Toast.makeText(this, "You are not connected to the Internet", Toast.LENGTH_LONG).show();
            try {
                if(new File(this.getFilesDir(),"recent.txt").exists()) {
                    ObjectInputStream inputStream=new ObjectInputStream(openFileInput("recent.txt"));
                    characters= (ArrayList<CharacterModel>) inputStream.readObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                characters.forEach(c->c.setImage(null));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fetchFavorites(){
        try {
            if(new File(this.getFilesDir(),"favorites.txt").exists()){
                ObjectInputStream inputStream = new ObjectInputStream(openFileInput("favorites.txt"));
                characters = (ArrayList<CharacterModel>) inputStream.readObject();
                inputStream.close();
            } else {
                characters.clear();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(!isNetworkAvailable()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                characters.forEach(c->c.setImage(null));
            }
        }
        }
    public void showCharacters(View v){
        String[] charactersArray = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            charactersArray = filteredCharacters();
        }
        ListView listView = findViewById(R.id.listofcharacters);
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, charactersArray);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {

            Intent i = new Intent(this, CharacterActivity.class);
            i.putExtra("clickedCharacter", (Parcelable) characters.get(position));
            startActivity(i);
        });
        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
        findViewById(R.id.inputText).setVisibility(View.INVISIBLE);
        findViewById(R.id.inputText).setActivated(false);
        v.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String[] filteredCharacters(){
        EditText textView = findViewById(R.id.inputText);
        Spinner spinner = findViewById(R.id.spinner);
        String inputText= textView.getText().toString();
        String inputSpinner = spinner.getSelectedItem().toString();

        if(!inputSpinner.equals("Status")){
            characters=characters.stream().filter(c->c.getStatus().equals(inputSpinner)).collect(Collectors.toList());
        }
        if(!inputText.equals("") && !inputText.equals("Episode Name...")){
            characters=characters.stream().filter(c->matchesEpisodes(c, inputText)).collect(Collectors.toList());
        }

        return characters.stream().map(CharacterModel::getName).toArray(String[]::new);
    }

    private boolean matchesEpisodes(CharacterModel c, String e){
        Pattern p = Pattern.compile(e, Pattern.CASE_INSENSITIVE);
        for (String s:c.getEpisodes()) {
            if (p.matcher(s).find()) {
                return true;
            }
        }
        return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}