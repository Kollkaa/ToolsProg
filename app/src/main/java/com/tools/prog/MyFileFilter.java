package com.tools.prog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

class MyFileNameFilter implements FilenameFilter{

    private String ext;

    public MyFileNameFilter(String ext){
        this.ext = ext.toLowerCase();
    }
    @Override
    public boolean accept(File dir, String name) {
        return true;
    }
}

