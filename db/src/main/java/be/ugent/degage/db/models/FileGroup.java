package be.ugent.degage.db.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public class FileGroup implements Iterable<File> {

    private int id;
    private List<File> files;

    public FileGroup(int id, List<File> files){
        this.id = id;
        this.files = files;
    }

    public FileGroup(int id){
        this.id = id;
        this.files = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int size(){
        return files.size();
    }

    @Override
    public Iterator<File> iterator() {
        return files.iterator();
    }

    public void addFile(File file){
        files.add(file);
    }

    public boolean removeFile(File file){
        return files.remove(file);
    }

    public List<File> getList() { return files; } //Because Scala remplating failed to recognize iterable...

    public List<File> copyList(){
        return new ArrayList<>(files); //deep copy
    }

    public File getFileWithId(int fileId){
        // Linear search is fast enough (<= 1k files)
        for(File file : files){
            if(file.getId() == fileId)
                return file;
        }
        return null;
    }
}
