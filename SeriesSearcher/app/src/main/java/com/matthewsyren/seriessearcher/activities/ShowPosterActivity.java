package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.models.ShowImage;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity displays the poster for a Show
 */

public class ShowPosterActivity
        extends AppCompatActivity {
    //View bindings
    @BindView(R.id.image_view_show_poster) ImageView mIvShowPoster;
    @Nullable
    @BindView(R.id.text_view_show_title) TextView mTvShowTitle;

    //Constants
    public static final String SHOW_KEY = "show_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_poster);
        ButterKnife.bind(this);

        //Makes the Activity transparent and closable on touch outside
        this.setFinishOnTouchOutside(true);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //Displays the Show's information
        displayShowInformation();
    }

    /**
     * Displays the Show's information
     */
    private void displayShowInformation(){
        //Fetches the Show from the Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Show show = null;

        if(bundle != null){
            show = bundle.getParcelable(SHOW_KEY);
        }

        //Displays a default image if the show doesn't have a poster or the user has enabled data saving mode, otherwise displays the Show's poster
        if(show != null){
            if(UserAccountUtilities.getDataSavingPreference(this) || show.getShowImageUrl() == null){
                //Displays a default image
                mIvShowPoster.setScaleType(ImageView.ScaleType.CENTER);
                mIvShowPoster.setImageResource(R.mipmap.ic_launcher);
            }
            else{
                //Initialises ShowImage
                ShowImage showImage = new ShowImage(getWindowManager(), this);

                //Loads the poster
                Picasso.with(this)
                        .load(show.getShowImageUrl())
                        .resize(showImage.getWidth(), showImage.getHeight())
                        .onlyScaleDown()
                        .into(mIvShowPoster);
            }

            //Displays the title of the Show
            if(mTvShowTitle != null){
                mTvShowTitle.setText(show.getShowTitle());
            }
        }
    }

    /**
     * Closes the Dialog (suppressed the unused warning as the onClick attribute is set in styles.xml, under the ShowPosterActivityOuterConstraintLayout style)
     */
    @SuppressWarnings("unused")
    public void closeDialog(View view){
        onBackPressed();
    }
}