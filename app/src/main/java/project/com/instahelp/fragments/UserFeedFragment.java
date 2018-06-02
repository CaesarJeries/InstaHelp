package project.com.instahelp.fragments;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.activities.AddPost;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.adapters.UserFeedAdapter;
import project.com.instahelp.interfaces.DisplayManager;
import project.com.instahelp.interfaces.LocationManager;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.User;


public class UserFeedFragment extends Fragment {

    public static final int ADD_POST_REQ = 42;
    private LocationManager locationManager;
    private ListView listView;
    private UserFeedAdapter adapter;
    private View layoutView;
    private DisplayManager displayManager;
    private project.com.instahelp.interfaces.LocalDataManager localDataManager = MainActivity.getLocalDataManager();
    private boolean initialized = false;


    public UserFeedFragment() {
        // Required empty public constructor
    }

    public void setAdapter(UserFeedAdapter adapter){
        this.adapter = adapter;
    }
    public void setDisplayManager(DisplayManager displayManager){
        this.displayManager = displayManager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_user_feed, container, false);
        locationManager = (LocationManager) getActivity();

        listView = (ListView) layoutView.findViewById(R.id.user_feed_list_view);
        listView.setAdapter(adapter);
        TextView emptyView = (TextView) layoutView.findViewById(R.id.empty_list_view);
        listView.setEmptyView( emptyView );


        final FloatingActionButton fab = (FloatingActionButton) layoutView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddPost.class);
                Bundle params = new Bundle();
                User u = localDataManager.getCurrentUser();
                params.putString(Defaults.PreferencesTags.USER_ID, u.getID());
                params.putString(Defaults.PreferencesTags.USER_NAME, u.getName());
                params.putString(Defaults.PreferencesTags.PHONE_NUMBER, u.getPhoneNumber());

                intent.putExtras(params);
                getActivity().startActivityForResult(intent, ADD_POST_REQ);
            }
        });

        return layoutView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStart(){
        super.onStart();


        if(!initialized){
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    listView.setSelection(adapter.getCount() - 1);
                }
            });



            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String postOwnerId = ((TextView)view.findViewById(R.id.row_post_user_id)).getText().toString();
                    String post_id = ((TextView)view.findViewById(R.id.row_post_post_id)).getText().toString();
                    if(currentUserIsPostOwner(postOwnerId)){
                        displayManager.viewPostAsOwner(post_id);
                    }else{
                        String viewer_id = localDataManager.getCurrentUser().getID();
                        displayManager.viewPostAsOther(viewer_id, post_id);
                    }
                }
            });
            initialized = true;
        }
    }

    private boolean currentUserIsPostOwner(String postOwnerId){
        String currentUserId = localDataManager.getCurrentUser().getID();
        return currentUserId.compareTo(postOwnerId) == 0;
    }


}
