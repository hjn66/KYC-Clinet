package com.areatak.kycclient;

import java.net.MalformedURLException;
import java.net.URL;

public class RegisterData {
    private String NationalID;
    private URL url;
    private String nonce;
    private String ticket;
    private String firstName;
    private String lastName;

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

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getNationalID() {
        return NationalID;
    }

    public void setNationalID(String nationalID) {
        NationalID = nationalID;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }



    public RegisterData(String nationalID, String nonce, String ticket, String firstName, String lastName) throws MalformedURLException {
        NationalID = nationalID;
        this.url = new URL("http://46.105.145.154:8003/registerQR");
        this.nonce = nonce;
        this.ticket = ticket;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
