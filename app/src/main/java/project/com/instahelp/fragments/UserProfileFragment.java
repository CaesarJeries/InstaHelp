package project.com.instahelp.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import project.com.instahelp.R;
import project.com.instahelp.activities.MainActivity;
import project.com.instahelp.interfaces.DataManager;
import project.com.instahelp.interfaces.LocalDataManager;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import utils.gui.CircleImageView;


public class UserProfileFragment extends Fragment {

    private static final int RESULT_LOAD_IMAGE = 23;

    private ServerComm comm;
    private CircleImageView profilePictureView;
    private TextView userNameView;
    private TextView phoneNumberView;
    String user_id;
    ImageView rankImageView;
    Bitmap rank_image;
    View layoutView;
    DataManager dataManager = MainActivity.getDataManager();
    LocalDataManager localDataManager = MainActivity.getLocalDataManager();
    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        initComm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        initView(v);
        // Inflate the layout for this fragment
        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap image = BitmapFactory.decodeFile(picturePath);
            updateUserImage(image);

        }
    }

    public void refresh(){
        User u = localDataManager.getCurrentUser();
        if(profilePictureView == null) return;
        profilePictureView.setImageBitmap(u.getProfilePicture());
        userNameView.setText(u.getName());
        phoneNumberView.setText(u.getPhoneNumber());
        rankImageView.setImageBitmap(User.getRankImage(u.getRank(), getResources()));

        profilePictureView.invalidate();
        userNameView.invalidate();
        phoneNumberView.invalidate();
        rankImageView.invalidate();
    }


    private void initView(View v) {
        layoutView = v;
        profilePictureView = (CircleImageView) v.findViewById(R.id.profile_picture);
        final ImageButton change_picture_icon = (ImageButton) v.findViewById((R.id.change_picture));

        final User u = localDataManager.getCurrentUser();
        if(u.getProfilePicture() != null) profilePictureView.setImageBitmap(u.getProfilePicture());

        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (change_picture_icon.getVisibility() == View.GONE) {
                    change_picture_icon.setVisibility(View.VISIBLE);
                } else {
                    change_picture_icon.setVisibility(View.GONE);
                }
            }
        });

        change_picture_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        userNameView = (TextView) v.findViewById(R.id.user_name);
        userNameView.setText(u.getName());

        phoneNumberView = (TextView) v.findViewById(R.id.phone_number);
        phoneNumberView.setText(u.getPhoneNumber());

        ImageButton userNameEdit = (ImageButton) v.findViewById(R.id.user_name_edit);
        ImageButton phoneNumberEdit = (ImageButton) v.findViewById(R.id.phone_number_edit);


        userNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.user_profile_dialog, null);
                EditText editText = (EditText) layout.findViewById(R.id.edit_text);
                editText.setHint(R.string.user_name_hint);
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(layout);

                builder.setMessage(R.string.user_name_edit_dialog)
                        .setTitle(R.string.user_name_edit_dialog_title);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.edit_text);
                        String newName = editText.getText().toString();
                        if(verifyUserName(newName)){
                            updateUserName(newName);
                            dialog.dismiss();
                        }else {
                            String error_msg = getResources().getString(R.string.update_user_name_dialog_error);
                            editText.setError(error_msg);
                        }
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });


        phoneNumberEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.user_profile_dialog, null);
                EditText editText = (EditText) layout.findViewById(R.id.edit_text);
                editText.setHint(R.string.phone_number);
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(layout);

                builder.setMessage(R.string.phone_number_edit_dialog)
                        .setTitle(R.string.phone_number_edit_dialog_title);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.edit_text);
                        String newNumber = editText.getText().toString();
                        updatePhoneNumber(newNumber);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
        rankImageView = (ImageView) v.findViewById(R.id.rank);
        View rankLayout = v.findViewById(R.id.rank_layout);
        user_id = u.getID();
        final User user = localDataManager.getCurrentUser();
        if(user != null){
            Bitmap image = User.getRankImage(user.getRank(), getActivity().getResources());
            rankImageView.setImageBitmap(image);
            rankLayout.setVisibility(View.VISIBLE);
            rankImageView.invalidate();
        }

        rankImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRankInfo(user.getRank());
            }
        });

    }

    private void showRankInfo(User.Rank rank){
        RankInfoFragment frag = new RankInfoFragment();
        frag.setRank(rank);
        frag.setResources(getResources());
        frag.show(getActivity().getSupportFragmentManager(), "RankInfoFragment");
    }

    private void initComm(){
        try{
            comm = ServerComm.getInstance();
        }catch (ServerComm.AppNotInitializedException e){
            Log.e("Firebase", e.getMessage());
        }
    }

    /**
     * Verify that name contains only alphabetic letters.
     * @param name - the chosen user name
     * @return true if the name only contains letters, false otherwise.
     */

    private boolean verifyUserName(String name){
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private void updateUserImage(Bitmap image){
        String user_id = localDataManager.getCurrentUser().getID();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Defaults.PreferencesTags.USER_PICTURE, User.encodeImage(image));
        editor.apply();
        comm.changeProfilePicture(user_id, image);
        profilePictureView.setImageBitmap(image);
    }

    private void updatePhoneNumber(String newNumber){
        String user_id = localDataManager.getCurrentUser().getID();
        localDataManager.updatePhoneNumber(newNumber);
        comm.updatePhoneNumber(user_id, newNumber);
        phoneNumberView.setText(newNumber);
        phoneNumberView.invalidate();
    }

    private void updateUserName(String newName){
        String user_id = localDataManager.getCurrentUser().getID();
        localDataManager.updateUserName(newName);
        comm.changeUserName(user_id, newName);
        userNameView.setText(newName);
        userNameView.invalidate();
    }
}
