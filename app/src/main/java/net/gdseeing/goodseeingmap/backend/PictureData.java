package net.gdseeing.goodseeingmap.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PictureData {
    private String pict_id;
    private String title;
    private float latitude;
    private float longitude;
    private String comment;
    private String url;
    private String user_id;

    public PictureData(String pict_id, String title,
                       float latitude, float longitude,
                       String comment, String url, String user_id){
        this.pict_id = pict_id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
        this.url = url;
        this.user_id = user_id;
    }
    public String json() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
    // Getter Methods

    public String getPict_id() {
        return pict_id;
    }

    public String getTitle() {
        return title;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getComment() {
        return comment;
    }

    public String getUrl() {
        return url;
    }

    public String getUser_id() {
        return user_id;
    }

    // Setter Methods

    public void setPict_id( String pict_id ) {
        this.pict_id = pict_id;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public void setLatitude( float latitude ) {
        this.latitude = latitude;
    }

    public void setLongitude( float longitude ) {
        this.longitude = longitude;
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public void setUser_id( String user_id ) {
        this.user_id = user_id;
    }
}
