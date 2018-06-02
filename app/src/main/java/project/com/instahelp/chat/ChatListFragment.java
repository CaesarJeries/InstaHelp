package project.com.instahelp.chat;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.BadgeManager;


public class ChatListFragment extends Fragment {
    private ListView listView;
    private static ChatListAdapter adapter;
    private static MainActivity.ChatListManager chatListManager = MainActivity.getChatListManager();
    private boolean startup = true;

    public ChatListFragment(){
        // required empty constructor.
    }

    public void setAdapter(BaseAdapter adapter){
        this.adapter = (ChatListAdapter) adapter;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layoutView = inflater.inflate(R.layout.fragment_chat_list, container, false);
        listView = (ListView) layoutView.findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        View emptyView = layoutView.findViewById(R.id.empty_list_view);
        listView.setEmptyView( emptyView );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String user_id = ((TextView) view.findViewById(R.id.user_id)).getText().toString();
                Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                intent.putExtra("other_id", user_id);
                startActivity(intent);
            }
        });
        registerForContextMenu(listView);

        return layoutView;
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
                MainActivity.getBadgeManager().hide(BadgeManager.Tab.CHAT);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, R.id.delete, Menu.NONE, "Delete");
        chat_id = ((TextView) v.findViewById(R.id.user_id)).getText().toString();
    }
    String chat_id;
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                chatListManager.deleteChat(chat_id);
                adapter.notifyDataSetChanged();
            }
        }
        return super.onContextItemSelected(item);
    }

}
