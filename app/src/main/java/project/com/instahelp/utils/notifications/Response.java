package project.com.instahelp.utils.notifications;

import java.util.Map;

public class Response extends Notification {

    public enum Permission{
        Granted,
        Denied
    }

    protected Permission permission;


    public Response(String id, String post_id, String from, String to, Response.Permission permission){
        super(id, post_id, from, to);
        this.permission = permission;
        setType(Type.Response);
    }

    public Permission getPermission(){
        return permission;
    }
    public void setPermission(Permission permission){
        this.permission = permission;
    }

    public Map<String, String> toMap(){
        Map<String, String> retval = super.toMap();
        retval.put("permission", permission.toString());
        return retval;
    }
}
