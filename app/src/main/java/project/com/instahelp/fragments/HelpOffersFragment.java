package project.com.instahelp.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

import project.com.instahelp.R;
import project.com.instahelp.activities.ViewPostAsOwnerActivity;
import project.com.instahelp.adapters.HelpOffersViewAdapter;
import project.com.instahelp.utils.Post;
import project.com.instahelp.utils.User;


public class HelpOffersFragment extends DialogFragment {

    private HelpOffersViewAdapter adapter;
    private View layoutView;
    private ListView listView;
    private Post post;
    Map<String, User> usersMap;

     public void setArgument(Post post){
        this.post = post;
    }

    public void setArgument(Map<String, User> map){
        this.usersMap = map;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String event_type = getResources().getString(R.string.help_offers_title);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        layoutView = inflater.inflate(R.layout.content_help_offers_fragment, null);
        listView = (ListView) layoutView.findViewById(R.id.list_view);

        adapter = new HelpOffersViewAdapter(this, post, usersMap, getActivity());
        listView.setAdapter(adapter);

        builder.setView(layoutView)
                .setTitle(event_type);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user_id = ((TextView) view.findViewById(R.id.user_id)).getText().toString();
                ((ViewPostAsOwnerActivity) getActivity()).viewProfileAsOther(user_id);
            }
        });


        return builder.create();
    }


}
