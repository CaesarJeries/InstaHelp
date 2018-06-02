package utils.location;

public class Coordinates{
    public static final Coordinates UNDEFINED = new Coordinates(null, null);
    private String longitude;
    private String latitude;

    public Coordinates(String lon, String lat){
        longitude = lon;
        latitude = lat;
    }

    public Coordinates(double lon, double lat){
        longitude = String.valueOf(lon);
        latitude = String.valueOf(lat);
    }

    public String getLongitude(){
        return longitude;
    }

    public String getLatitude(){
        return latitude;
    }

    public boolean isDefined(){
        return (latitude != null) && (longitude != null);
    }
}
