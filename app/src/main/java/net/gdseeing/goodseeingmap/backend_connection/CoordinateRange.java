package net.gdseeing.goodseeingmap.backend_connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoordinateRange {
    private float latitude_h;
    private float latitude_l;
    private float longitude_h;
    private float longitude_l;

    public String json() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
    // Getter Methods

    public float getLatitude_h() {
        return latitude_h;
    }

    public float getLatitude_l() {
        return latitude_l;
    }

    public float getLongitude_h() {
        return longitude_h;
    }

    public float getLongitude_l() {
        return longitude_l;
    }

    // Setter Methods

    public void setLatitude_h( float latitude_h ) {
        this.latitude_h = latitude_h;
    }

    public void setLatitude_l( float latitude_l ) {
        this.latitude_l = latitude_l;
    }

    public void setLongitude_h( float longitude_h ) {
        this.longitude_h = longitude_h;
    }

    public void setLongitude_l( float longitude_l ) {
        this.longitude_l = longitude_l;
    }
}
