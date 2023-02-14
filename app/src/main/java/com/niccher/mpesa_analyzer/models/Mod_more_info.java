package com.niccher.mpesa_analyzer.models;

public class Mod_more_info {
    public String name_title,name_desc;

    public Mod_more_info(String name_title, String name_desc) {
        this.name_title = name_title;
        this.name_desc = name_desc;
    }

    public String getName_title() {
        return name_title;
    }

    public void setName_title(String name_title) {
        this.name_title = name_title;
    }

    public String getName_desc() {
        return name_desc;
    }

    public void setName_desc(String name_desc) {
        this.name_desc = name_desc;
    }
}
