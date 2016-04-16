package com.example.jvenditti.yelpoauthsample;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class YelpGalleryFragment extends Fragment {

    private static String LOG_TAG = YelpGalleryFragment.class.getSimpleName();

    private static Integer GRID_COLUMN_COUNT = 3;   // TODO: Should be set based on size of display
    private static String FIXED_QUERY = "food";     // TODO: Should be passed in from user input

    private RecyclerView mRecyclerView;
    private YelpItemAdapter mItemAdapter;
    private GridLayoutManager mLayoutManager;
    private List<GalleryItem> mItems = new ArrayList<>();
    private Snackbar mSnackbar;
    private Toast mToast;

    private Boolean mIsReadyToLoadMoreItems = true;
    private int mPastVisibleItems = 0;
    private int mVisibleItemCount = 0;
    private int mTotalItemCount = 0;

    public static YelpGalleryFragment newInstance() {
        Log.d(LOG_TAG, "newInstance");
        return new YelpGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        requestYelpItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreateView");

        View v = inflater.inflate(R.layout.fragment_yelp_gallery, container, false);

        mLayoutManager = new GridLayoutManager(getActivity(), GRID_COLUMN_COUNT);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_yelp_gallery_recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {

                    mVisibleItemCount = mLayoutManager.getChildCount();
                    mTotalItemCount = mLayoutManager.getItemCount();
                    mPastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (mIsReadyToLoadMoreItems) {
                        if ((mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
                            Log.d(LOG_TAG, "Request more items");
                            Log.d(LOG_TAG, "------ visible: " + mVisibleItemCount);
                            Log.d(LOG_TAG, "------ total: " + mTotalItemCount);
                            Log.d(LOG_TAG, "------ past visible: " + mPastVisibleItems);
                            mIsReadyToLoadMoreItems = false;
                            requestYelpItems();
                        }
                    }
                }
            }
        });
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    public void requestYelpItems() {
        Log.d(LOG_TAG, "requestYelpItems");
        FetchItemsTask task = new FetchItemsTask(getActivity());
        task.execute(FIXED_QUERY, String.valueOf(mTotalItemCount));
    }

    public Integer getYelpItemAdapterCount() {
        return mItemAdapter.getItemCount();
    }

    private void setupAdapter() {
        if (isAdded()) {
            Log.d(LOG_TAG, "setupAdapter");
            mItemAdapter = new YelpItemAdapter(mItems);
            mRecyclerView.setAdapter(mItemAdapter);
        }
    }

    private void appendItems(List<GalleryItem> items) {
        if (isAdded()) {
            Log.d(LOG_TAG, "appendItems: " + items.size());
            if (items.size() > 0) {
                mItems.addAll(items);
                mItemAdapter.notifyDataSetChanged();
                showNewItemsMessage(items.size(), mItems.size());
            }
            mIsReadyToLoadMoreItems = true;
        }
    }

    private void showNewItemsMessage(Integer count, Integer total) {
        Log.d(LOG_TAG, "showNewItemsMessage: " + count);
        String message = getString(R.string.message_adding_new_items, count, total);
        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }
        mSnackbar = Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG);
        mSnackbar.show();
    }

    private void showClickedName(String name) {
        Log.d(LOG_TAG, "showClickedName: " + name);
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), name, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private class FetchItemsTask extends AsyncTask<String, Void, List<GalleryItem>> {

        private Context mContext;

        public FetchItemsTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<GalleryItem> doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground");
            String query = params[0];
            String offset = params[1];
            return new YelpFetcher().fetchItems(mContext, query, offset);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            Log.d(LOG_TAG, "onPostExecute");
            if (getYelpItemAdapterCount() == 0) {
                mItems = items;
                setupAdapter();
            } else {
                appendItems(items);
            }
        }
    }

    private class YelpItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mImageView;

        public YelpItemHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.fragment_yelp_gallery_image_view);
            mImageView.setOnClickListener(this);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            Picasso.with(getActivity())
                    .load(galleryItem.getImageUrl())
                    .into(mImageView);
        }

        @Override
        public void onClick(View view) {
            GalleryItem item = mItems.get(getAdapterPosition());
            showClickedName(item.getName());
        }
    }

    private class YelpItemAdapter extends RecyclerView.Adapter<YelpItemHolder> {

        private List<GalleryItem> mGalleryItems;

        public YelpItemAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public YelpItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new YelpItemHolder(view);
        }

        @Override
        public void onBindViewHolder(YelpItemHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
