package com.niccher.mpesa_analyzer.models;

public class Mod_Sms_Cat_Spinner {
    public String spinner_text;
    public int spinner_img;

    public Mod_Sms_Cat_Spinner(String spinner_text, int spinner_img) {
        this.spinner_text = spinner_text;
        this.spinner_img = spinner_img;
    }

    public String getSpinner_text() {
        return spinner_text;
    }

    public void setSpinner_text(String spinner_text) {
        this.spinner_text = spinner_text;
    }

    public int getSpinner_img() {
        return spinner_img;
    }

    public void setSpinner_img(int spinner_img) {
        this.spinner_img = spinner_img;
    }
}
