package applicationapi;
/*
 * Handles materials and equipment being used
 * on construction projects.
 */
public interface MaterialEquipmentAPI {
//	Adds a material to a project.
	String addMaterial(
            String projectId,
            String materialName,
            double quantity);
	
//	Updates the quantity of a material.
	boolean updateMaterialQuantity(
            String materialId,
            double quantity);
	
//	Adds equipment to a project.
	 String addEquipment(
	            String projectId,
	            String equipmentName);
	 
//	 Removes equipment from a project.
	 boolean removeEquipment(String equipmentId);
	 
//	 Retrieves the materials and equipment
	 String getProjectMaterialsAndEquipment(
	            String projectId);
}
