package com.example.socialapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.socialapp.model.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class YourPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView rvPosts;
    private ArrayList<Post> list = new ArrayList<>();
    private YourPostsAdapter ypostsAdapter;
    private SwipeRefreshLayout srlYourPosts;

    public YourPostsFragment() {
        // Required empty public constructor
    }
    public static YourPostsFragment newInstance() {
        YourPostsFragment fragment = new YourPostsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_your_posts, container, false);

        getYourPosts();

        rvPosts = view.findViewById(R.id.rv_your_posts_list);
        srlYourPosts = view.findViewById(R.id.srl_your_posts);

        rvPosts.setHasFixedSize(true);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        ypostsAdapter = new YourPostsAdapter(list,getContext());
        rvPosts.setAdapter(ypostsAdapter);
        ypostsAdapter.setOnItemClickCallback(new OnItemClickCallback() {
            @Override
            public void onItemClicked(Post data) {
                showPost(data);
            }
        });

        srlYourPosts.setColorSchemeResources(R.color.colorAccent);
        srlYourPosts.setOnRefreshListener(this);

        return view;
    }

    private void showPost(Post post) {
        Intent postDetailIntent = new Intent(getContext(), PostActivity.class);
        postDetailIntent.putExtra("post_title", post.getId_user());
        postDetailIntent.putExtra("post_caption", post.getPost());
        startActivity(postDetailIntent);
    }

    private void getYourPosts(){
        class GetYourPosts extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getContext(),"Fetching...","Wait...",false,false);
            }



            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                showPosts(s);
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String,String> params = new HashMap<>();

                Context appContext = LoginActivity.getAppContext();
                SharedPreferences loginData = appContext.getSharedPreferences("Login", MODE_PRIVATE);

                params.put("id_user",loginData.getString("id_user","1"));
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest("http://frozenbits.tech/socialAppWS/index.php/DataPost/getPostByUserId", params);

                return s;
            }
        }
        GetYourPosts gyp = new GetYourPosts();
        gyp.execute();

    }

    private void showPosts(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean status= jsonObject.getBoolean("status");
            String message= jsonObject.getString("message");
            if(status){
                JSONArray result = jsonObject.getJSONArray("data");
                for (int i = 0; i < result.length(); i++) {
                    Post post = new Post();
                    JSONObject pst = result.getJSONObject(i);
                    post.setId_post(pst.getString("id_post"));
                    post.setId_user(pst.getString("id_user"));
                    post.setPost(pst.getString("post"));
                    post.setTime(pst.getString("waktu"));
                    post.setName(pst.getString("nama"));
                    list.add(post);
                }
                ypostsAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
//                getActivity().finish();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        list.clear();
        getYourPosts();
        srlYourPosts.setRefreshing(false);
    }

}