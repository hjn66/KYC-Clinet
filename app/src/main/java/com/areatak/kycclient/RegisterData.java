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
    private String encodedPhoto;
    private String birthDate;
    private String publicKey;

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthdate) {
        this.birthDate = birthdate;
    }

    public String getEncodedPhoto() {
        return encodedPhoto;
    }

    public void setEncodedPhoto(String encodedPhoto) {
        this.encodedPhoto = encodedPhoto;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }



    public RegisterData(String url, String nationalID, String firstName, String lastName,String birthDate, String encodedPhoto,String publicKey, String nonce, String ticket) throws MalformedURLException {
        NationalID = nationalID;
        this.url = new URL(url);
        this.nonce = nonce;
        this.ticket = ticket;
        this.firstName = firstName;
        this.lastName = lastName;
        this.encodedPhoto = encodedPhoto;
        this.birthDate = birthDate;
        this.publicKey = publicKey;
    }

}
