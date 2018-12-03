package com.areatak.saderat;

import java.net.MalformedURLException;
import java.net.URL;

public class LoginData {
    private URL url;
    private String signedNonce;
    private String ticket;
    private int GUID;
    private String firstName;
    private String lastName;
    private String encodedPhoto;


    public String getEncodedPhoto() {
        return encodedPhoto;
    }

    public void setEncodedPhoto(String encodedPhoto) {
        this.encodedPhoto = encodedPhoto;
    }

    public int getGUID() {
        return GUID;
    }

    public void setGUID(int GUID) {
        this.GUID = GUID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSignedNonce() {
        return signedNonce;
    }

    public void setSignedNonce(String signedNonce) {
        this.signedNonce = signedNonce;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }


    public LoginData(String url, int GUID, String firstName, String lastName, String encodedPhoto, String signedNonce, String ticket) throws MalformedURLException {
        this.url = new URL(url);
        this.GUID = GUID;
        this.signedNonce = signedNonce;
        this.ticket = ticket;
        this.firstName = firstName;
        this.lastName = lastName;
        this.encodedPhoto = encodedPhoto;
    }
}
