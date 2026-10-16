package applicationapi;

import java.util.List;
import java.util.Map;

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
//	Removes a material from a project.
	boolean removeMaterial(String materialId);
	
//	Adds equipment to a project.
	String addEquipment(
	            String projectId,
	            String equipmentName);
	 
//	 Removes equipment from a project.
	 boolean removeEquipment(String equipmentId);
	 
//	 Retrieves the materials and equipment
	// Retrieves materials and equipment assigned to a project.
	 Map<String, List<Map<String, Object>>> getProjectMaterialsAndEquipment(
	         String projectId);
}
