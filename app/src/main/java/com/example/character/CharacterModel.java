package com.example.character;

import android.os.Parcel;
import android.os.Parcelable;


import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CharacterModel implements Parcelable, Serializable {
    private final String name;
    private final String status;
    private final String url;
    private final List<String> episodes;
    private final String location;
    private final String species;
    private final String gender;
    private final String origin;
    private String image;

    public CharacterModel(String name, String status, String url,
                          List<String> episodes, String location, String species,
                          String gender, String origin, String image) {
        this.name = name;
        this.status = status;
        this.url = url;
        this.episodes = episodes;
        this.location = location;
        this.species = species;
        this.gender = gender;
        this.origin = origin;
        this.image = image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private CharacterModel(Parcel in) {
        name=in.readString();
        status=in.readString();
        url=in.readString();
        episodes=new ArrayList<>();
        in.readList(episodes, String.class.getClassLoader());
        location=in.readString();
        species=in.readString();
        gender=in.readString();
        origin=in.readString();
        image=in.readString();
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getEpisodes() {
        return episodes;
    }

    public String getLocation() {
        return location;
    }

    public String getSpecies() {
        return species;
    }

    public String getGender() {
        return gender;
    }

    public String getOrigin() {
        return origin;
    }

    //probably not useful
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(url);
        dest.writeList(episodes);
        dest.writeString(location);
        dest.writeString(species);
        dest.writeString(gender);
        dest.writeString(origin);
        dest.writeString(image);
    }

    public static final Parcelable.Creator<CharacterModel> CREATOR = new Parcelable.Creator<CharacterModel>() {
        public CharacterModel createFromParcel(Parcel in) {
            return new CharacterModel(in);
        }
        public CharacterModel[] newArray(int size) {
            return new CharacterModel[size];
        }
    };

    @Override
    public String toString() {
        return  "Gender: "+ getGender()+"\n"+
                "Species: " + getSpecies()+ "\n"+
                "Status: " + getStatus()+"\n"+
                "Number of episodes: " + getEpisodes().size()+"\n"+
                "Origin: "+ getOrigin()+"\n"+
                "Current Location: "+ getLocation();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj.getClass()!=CharacterModel.class) return false;
        return this.getName().equals(((CharacterModel)obj).getName()) && this.getEpisodes().equals(((CharacterModel)obj).getEpisodes());
    }
}
