package project.com.instahelp.interfaces;

import com.firebase.client.Query;

public interface RefManager {
    Query getPostRef();
    Query getUserRef();
}
