package be.ugent.degage.db.models;

public class File {
    private int fileId;
    private String path;

    private String fileName;
    private String contentType;

    public File(int fileId, String path, String fileName, String contentType) {
        this.fileId = fileId;
        this.path = path;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public int getId(){
        return fileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean equals(Object o){
        if(o == null)                return false;
        if(!(o instanceof File)) return false;

        File other = (File) o;
        return other.getId() == this.getId();
    }

    @Override
    public int hashCode(){
        return this.getId();
    }
}
