package com.usman.smartads;

public class Ad_ID {
    private String ID;
    private boolean PRELOAD;

    public Ad_ID(){}

    public Ad_ID(String ID, boolean PRELOAD) {
        this.ID = ID;
        this.PRELOAD = PRELOAD;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isPRELOAD() {
        return PRELOAD;
    }

    public void setPRELOAD(boolean PRELOAD) {
        this.PRELOAD = PRELOAD;
    }
}
