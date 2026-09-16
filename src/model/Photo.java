package model;

public class Photo {

    private String photoId;
    private String projectId;
    private String photoLocation;

    public Photo(String photoId, String projectId, String photoLocation) {
        this.photoId = photoId;
        this.projectId = projectId;
        this.photoLocation = photoLocation;
    }

//  get method
    public String getPhotoId() {
        return photoId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getPhotoLocation() {
        return photoLocation;
    }
}