package com.example.jvenditti.yelpoauthsample;

public class GalleryItem {

    private String mId;
    private String mName;
    private String mImageUrl;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return mName;
    }
}
