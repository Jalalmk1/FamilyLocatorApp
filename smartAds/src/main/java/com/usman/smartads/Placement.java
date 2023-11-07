package com.usman.smartads;

public class Placement {
    private String AD_NETWORK;
    private String CTR;
    private boolean SHOW_LOADING;
    private boolean RELOAD;

    public Placement(){}

    public Placement(String AD_NETWORK, String CTR, boolean SHOW_LOADING, boolean RELOAD) {
        this.AD_NETWORK = AD_NETWORK;
        this.CTR = CTR;
        this.SHOW_LOADING = SHOW_LOADING;
        this.RELOAD = RELOAD;
    }

    public String getAD_NETWORK() {
        return AD_NETWORK;
    }

    public void setAD_NETWORK(String AD_NETWORK) {
        this.AD_NETWORK = AD_NETWORK;
    }

    public String getCTR() {
        return CTR;
    }

    public void setCTR(String CTR) {
        this.CTR = CTR;
    }

    public boolean isSHOW_LOADING() {
        return SHOW_LOADING;
    }

    public void setSHOW_LOADING(boolean SHOW_LOADING) {
        this.SHOW_LOADING = SHOW_LOADING;
    }

    public boolean isRELOAD() {
        return RELOAD;
    }

    public void setRELOAD(boolean RELOAD) {
        this.RELOAD = RELOAD;
    }
}
