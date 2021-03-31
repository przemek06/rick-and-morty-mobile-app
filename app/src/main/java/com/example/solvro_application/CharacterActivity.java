package com.example.solvro_application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.character.CharacterModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class CharacterActivity extends AppCompatActivity {
    CharacterModel character;
    private boolean isFavorite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        Intent i = getIntent();
        TextView text= findViewById(R.id.textView);
        character = i.getParcelableExtra("clickedCharacter");
        text.setText(character.toString());
        setTitle(character.getName());

        try {
            ArrayList<CharacterModel> characterModels = new ArrayList<>();
            if(new File(this.getFilesDir(),"recent.txt").exists()) {
                ObjectInputStream inputStream= new ObjectInputStream(openFileInput("recent.txt"));
                characterModels = (ArrayList<CharacterModel>) inputStream.readObject();
                inputStream.close();
            }
            characterModels.add(character);
            ObjectOutputStream outputStream = new ObjectOutputStream(openFileOutput("recent.txt", MODE_PRIVATE));
            outputStream.writeObject(characterModels);
            outputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(character.getImage()!=null) {
            ImageView imageView=findViewById(R.id.imageView);
            new DownloadImageTask(imageView).execute(character.getImage());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.character_menu, menu);
        try {
            if(isInFavorites()) {
                isFavorite=true;
                menu.findItem(R.id.action_add_to_favorites).setIcon(R.drawable.ic_heart_full);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_to_favorites) {
            if (!(isFavorite)) {
                isFavorite = true;
                item.setIcon(R.drawable.ic_heart_full);
                try {
                    addToFavorites();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else{
                isFavorite = false;
                item.setIcon(R.drawable.ic_heart_border);
                try {
                    deleteFromFavorites();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
    static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            bmImage.setImageBitmap(result);
        }
    }

    private void addToFavorites() throws IOException, ClassNotFoundException {
        ArrayList<CharacterModel> characterModels = new ArrayList<>();
        if(new File(this.getFilesDir(),"favorites.txt").exists()){
            ObjectInputStream inputStream= new ObjectInputStream(openFileInput("favorites.txt"));
            characterModels= (ArrayList<CharacterModel>) inputStream.readObject();
            inputStream.close();
        }
        ObjectOutputStream outputStream = new ObjectOutputStream(openFileOutput("favorites.txt", MODE_PRIVATE));
        characterModels.add(character);
        outputStream.writeObject(characterModels);
        outputStream.close();
    }

    private void deleteFromFavorites() throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream= new ObjectInputStream(openFileInput("favorites.txt"));
        ArrayList<CharacterModel> characterModels = (ArrayList<CharacterModel>) inputStream.readObject();
        characterModels.remove(character);
        inputStream.close();
        ObjectOutputStream outputStream = new ObjectOutputStream(openFileOutput("favorites.txt", MODE_PRIVATE));
        outputStream.writeObject(characterModels);
        outputStream.close();
        if(characterModels.isEmpty()) {
            File file = new File(getFilesDir(), "favorites.txt");
            file.delete();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isInFavorites() throws IOException, ClassNotFoundException {
        if(new File(this.getFilesDir(),"favorites.txt").exists()){
            ObjectInputStream inputStream= new ObjectInputStream(openFileInput("favorites.txt"));
            ArrayList<CharacterModel> characterModels= (ArrayList<CharacterModel>) inputStream.readObject();
            inputStream.close();
            return  characterModels.contains(character);
        }
        return false;
    }
}