package com.tools.prog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class MyFileFilter implements FilenameFilter {
    private String ext;

    public MyFileFilter(String ext) {
        this.ext = ext;
    }

    public boolean accept(File dir, String name) {
        int p = name.indexOf(ext);
        return ((p >= 0) && (p + ext.length() == name.length()));
    }
}

