package com.example.jvenditti.yelpoauthsample;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;

public class YelpFetcher {

    private static final String LOG_TAG = YelpFetcher.class.getSimpleName();

    // OAUTH VALUES
    private static final String YELP_CONSUMER_KEY = "...";      // TODO: ADD YOUR YELP VALUE
    private static final String YELP_CONSUMER_SECRET = "...";   // TODO: ADD YOUR YELP VALUE
    private static final String YELP_TOKEN = "...";             // TODO: ADD YOUR YELP VALUE
    private static final String YELP_TOKEN_SECRET = "...";      // TODO: ADD YOUR YELP VALUE

    // MAX YELP SEARCH REQUEST LIMIT
    public static final Integer MAX_YELP_SEARCH_REQUEST_LIMIT = 20;

    // INTENT ACTION
    public static final String ACTION_COULD_NOT_LOAD_YELP_DATA = "action_could_not_load_yelp_data";

    // ERROR VALUES
    public static final String ERROR_CODE_KEY = "error_code_key";
    public static final int ERROR_CODE_UNKNOWN_HOST = -1;
    public static final int ERROR_CODE_IO_EXCEPTION = -2;

    public List<GalleryItem> fetchItems(Context context, String query, String offset) {

        Log.d(LOG_TAG, "fetchItems: " + query + " | " + offset);

        List<GalleryItem> items = new ArrayList<>();

        YelpAPIFactory apiFactory = new YelpAPIFactory(YELP_CONSUMER_KEY, YELP_CONSUMER_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        // general params
        params.put("term", query);
        params.put("limit", Integer.toString(MAX_YELP_SEARCH_REQUEST_LIMIT));
        params.put("offset", offset);

        // locale params
        params.put("lang", "en");

        Call<SearchResponse> call = yelpAPI.search("San Francisco", params);

        try {

            Response<SearchResponse> response = call.execute();
            SearchResponse searchResponse = response.body();

            if (searchResponse.total() > 0) {
                ArrayList<Business> businesses = searchResponse.businesses();
                parseItems(items, businesses);
            }

        } catch (UnknownHostException e) {

            Intent intent = new Intent(ACTION_COULD_NOT_LOAD_YELP_DATA);
            intent.putExtra(ERROR_CODE_KEY, ERROR_CODE_UNKNOWN_HOST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            //e.printStackTrace();

        } catch (IOException e) {

            Intent intent = new Intent(ACTION_COULD_NOT_LOAD_YELP_DATA);
            intent.putExtra(ERROR_CODE_KEY, ERROR_CODE_IO_EXCEPTION);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            //e.printStackTrace();
        }

        return items;
    }

    private void parseItems(List<GalleryItem> items, ArrayList<Business> businesses) {
        Log.d(LOG_TAG, "parseItems");
        for (int i = 0; i < businesses.size(); i++) {
            GalleryItem item = new GalleryItem();
            item.setId(businesses.get(i).id());
            item.setName(businesses.get(i).name());
            item.setImageUrl(businesses.get(i).imageUrl());
            items.add(item);
        }
    }
}
