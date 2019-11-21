package com.dhananjay.myapplication.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        Query recentPostsQuery = databaseReference.child("posts");
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
