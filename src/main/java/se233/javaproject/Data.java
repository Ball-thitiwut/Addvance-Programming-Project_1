package se233.javaproject;

import java.io.File;
import java.util.List;

public class Data {
    private File selected;
    private static final Data instance = new Data();
    public static Data getInstance(){return instance;}
    private List<File> files;
    public List<File> getFiles() {return files;}
    public void setFiles(List<File> files){this.files = files;}
    public Data(){}
}
