package com.codepath.flicks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.flicks.models.Config;
import com.codepath.flicks.models.Movie;
import com.codepath.flicks.models.MovieTrailerActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    //base url for api
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //parameter name for api key
    public final static String API_KEY_PARAM = "api_key";

    Movie movie;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivTrailer;

    //instance fields
    AsyncHttpClient client;
    //image config
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        ivTrailer = (ImageView) findViewById(R.id.ivTrailer);
        //initialize client
        client = new AsyncHttpClient();

        //retrieve movie
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        config = (Config) Parcels.unwrap(getIntent().getParcelableExtra("IMAGE_URL"));
        Log.d("MovieDetailActivity", String.format("Showing details for '%s'", movie.getTitle()));

        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        String imageUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
        int placeholderId = R.drawable.flicks_backdrop_placeholder;
        //load image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(placeholderId)
                .error(placeholderId)
                .bitmapTransform(new RoundedCornersTransformation(this, 15, 0))
                .into(ivTrailer);

        //have to divide by 2 because average is 0..10
        float voteAverage = Float.parseFloat(movie.getVoteAverage());
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
    }

    public void getMovieId(View view) {
        String url = API_BASE_URL + "/movie/" + movie.getId() + "/videos\n";
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.youtube_api_key));
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //put results into list of movies
                try {
                    JSONArray results = response.getJSONArray("results");
                    JSONObject object = results.getJSONObject(0);
                    String key = object.getString("key");
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    intent.putExtra("VIDEO_KEY",key);
                    startActivity(intent);
                    Log.i("MovieDetailsActivity", "Loaded video id");
                } catch (JSONException e) {
                    logError("Failed to parse movie details", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true);
            }
        });
    }

    private void logError(String message, Throwable error, boolean alertUser) {
        Log.e("MovieDetailsActivity",message,error);
        if(alertUser) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
