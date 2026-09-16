package model;

public class Material {

    private String materialId;
    private String projectId;
    private String materialName;
    private double quantity;

    public Material(String materialId, String projectId, String materialName, double quantity) {
        this.materialId = materialId;
        this.projectId = projectId;
        this.materialName = materialName;
        this.quantity = quantity;
    }

//  get method
    public String getMaterialId() {
        return materialId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getMaterialName() {
        return materialName;
    }
    public double getQuantity() {
        return quantity;
    }

//  set Method
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
