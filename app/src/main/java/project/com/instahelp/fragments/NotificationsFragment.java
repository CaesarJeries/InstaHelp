package project.com.instahelp.fragments;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.adapters.NotificationsAdapter;
import project.com.instahelp.interfaces.BadgeManager;


public class NotificationsFragment extends Fragment {

    private ListView listView;
    private NotificationsAdapter adapter;
    private boolean startup = true;


    public NotificationsFragment(){
        // Required default constructor.
    }

    public void setAdapter(BaseAdapter adapter){
        this.adapter = (NotificationsAdapter) adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layoutView = inflater.inflate(R.layout.fragment_notifications, container, false);
        listView = (ListView) layoutView.findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        View emptyView = layoutView.findViewById(R.id.empty_list_view);
        listView.setEmptyView( emptyView );

        return layoutView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startup = false;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible){
            if(startup == false) {
                MainActivity.getBadgeManager().hide(BadgeManager.Tab.NOTIFICATIONS);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(adapter.getCount() - 1);
            }
        });

    }
}
