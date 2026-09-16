package model;

public class Equipment {

    private String equipmentId;
    private String projectId;
    private String equipmentName;

    public Equipment(String equipmentId, String projectId, String equipmentName) {
        this.equipmentId = equipmentId;
        this.projectId = projectId;
        this.equipmentName = equipmentName;
    }

//  get method
    public String getEquipmentId() {
        return equipmentId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getEquipmentName() {
        return equipmentName;
    }
}
