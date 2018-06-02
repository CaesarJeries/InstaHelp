package project.com.instahelp.fragments;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.adapters.MyRequestsAdapter;
import project.com.instahelp.adapters.PendingRequestsAdapter;
import project.com.instahelp.interfaces.DisplayManager;
import project.com.instahelp.interfaces.LocalDataManager;


public class RequestsFragment extends Fragment {

    public enum Position{
        MY_REQUESTS,
        PENDING_REQUESTS;
        public static Position valueOf(int index){
            if(index == 0) return MY_REQUESTS;
            return PENDING_REQUESTS;
        }
    }

    LocalDataManager localDataManager = MainActivity.getLocalDataManager();
    DisplayManager displayManager;
    private View layoutView;
    ListView listView;
    Spinner spinner;
    Position spinnerPosition;
    ArrayAdapter<CharSequence> adapter;
    private boolean initialized = false;

    MyRequestsAdapter myRequestsAdapter;
    PendingRequestsAdapter pendingRequestsAdapter;

    public RequestsFragment() {
        // Required empty public constructor
    }

    public void setAdapter(Position position, BaseAdapter adapter){
        switch (position){
            case MY_REQUESTS:{
                myRequestsAdapter = (MyRequestsAdapter) adapter;
                break;
            }
            case PENDING_REQUESTS:{
                pendingRequestsAdapter = (PendingRequestsAdapter) adapter;
                break;
            }
        }
    }

    public void setDisplayManager(DisplayManager displayManager){
        this.displayManager = displayManager;
    }


    @Override
    public void onStart(){
        super.onStart();
        if(initialized == false){
            if(myRequestsAdapter != null){
                myRequestsAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        listView.setSelection(myRequestsAdapter.getCount() -1);
                    }
                });
            }
            if(pendingRequestsAdapter != null){
                pendingRequestsAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        listView.setSelection(pendingRequestsAdapter.getCount() - 1);
                    }
                });
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String post_id = ((TextView)view.findViewById(R.id.row_post_post_id)).getText().toString();
                    switch(spinnerPosition){
                        case MY_REQUESTS:{
                            displayManager.viewPostAsOwner(post_id);
                            break;
                        }
                        case PENDING_REQUESTS:{
                            String viewer_id = localDataManager.getCurrentUser().getID();
                            displayManager.viewPostAsOther(viewer_id, post_id);
                            break;
                        }
                    }
                }
            });
            initialized = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layoutView = inflater.inflate(R.layout.fragment_requests, container, false);
        spinner = (Spinner) layoutView.findViewById(R.id.requests_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.requests_array, android.R.layout.simple_spinner_item);
        listView = (ListView) layoutView.findViewById(R.id.listview);
        initSpinner();

        return layoutView;
    }


    private void loadMyRequests(){
        listView.setAdapter(myRequestsAdapter);
        TextView emptyView = (TextView) layoutView.findViewById(R.id.empty_list_view);
        listView.setEmptyView( emptyView );
    }

    private void loadPendingRequests(){
        listView.setAdapter(pendingRequestsAdapter);
        TextView emptyView = (TextView) layoutView.findViewById(R.id.empty_list_view);
        listView.setEmptyView( emptyView );
    }

    private void initSpinner(){
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinnerPosition = Position.MY_REQUESTS;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(Position.valueOf(position)){
                    case MY_REQUESTS:{
                        loadMyRequests();
                        spinnerPosition = Position.MY_REQUESTS;
                        break;
                    }
                    case PENDING_REQUESTS:{
                        loadPendingRequests();
                        spinnerPosition = Position.PENDING_REQUESTS;
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
