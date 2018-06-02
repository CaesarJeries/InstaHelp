package project.com.instahelp.fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.utils.User;


public class RankInfoFragment extends DialogFragment {
    private View layoutView;
    private TextView contentView;
    private ImageView rankView;
    private User.Rank rank;
    private Resources resources;

    public void setRank(User.Rank rank){
        this.rank = rank;
    }

    public void setResources(Resources resources){
        this.resources = resources;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String event_type = getResources().getString(R.string.rank_info_title);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        layoutView = inflater.inflate(R.layout.rank_info_fragment, null);
        contentView = (TextView) layoutView.findViewById(R.id.content);
        rankView = (ImageView) layoutView.findViewById(R.id.rank_image);

        setContentText();

        builder.setView(layoutView)
                .setTitle(event_type);


        return builder.create();
    }

    private void setContentText(){
        String text = null;
        Bitmap image = null;
        switch(rank){
            case Unranked:{
                text = "Current rank : " + rank.toString() + "\nStart helping people to level up!";
                image = BitmapFactory.decodeResource(resources, R.drawable.unranked);
                break;
            }

            case Bronze:{
                text = "Current rank : " + rank.toString() + "\nYou earned this rank by helping " +
                    User.BRONZE_THRESHOLD + " people.";
                image = BitmapFactory.decodeResource(resources, R.drawable.bronze);
                break;
            }
            case Silver:{
                text = "Current rank : " + rank.toString() + "\nYou earned this rank by helping " +
                        User.SILVER_THRESHOLD + " people.";
                image = BitmapFactory.decodeResource(resources, R.drawable.silver);
                break;
            }
            case Gold:{
                text = "Current rank : " + rank.toString() + "\nYou earned this rank by helping " +
                        User.GOLD_THRESHOLD + " people.";
                image = BitmapFactory.decodeResource(resources, R.drawable.gold);
                break;
            }
            case Platinum:{
                text = "Current rank : " + rank.toString() + "\nYou earned this rank by helping " +
                        User.PLATINUM_THRESHOLD + " people.";
                image = BitmapFactory.decodeResource(resources, R.drawable.platinum);
                break;
            }

        }

        rankView.setImageBitmap(image);
        contentView.setText(text);
    }

}
