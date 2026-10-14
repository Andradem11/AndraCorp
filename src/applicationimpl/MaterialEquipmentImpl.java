
package applicationimpl;

import applicationapi.MaterialEquipmentAPI;
import storageimpl.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/*
 * Implementation of materials and equipment.
 * Uses SQLite to store materials and equipment
 * assigned to construction projects.
 */
public class MaterialEquipmentImpl
        implements MaterialEquipmentAPI {

    // Adds a material to a project.
    @Override
    public String addMaterial(
            String projectId,
            String materialName,
            double quantity) {

        if (projectId == null || projectId.isBlank() ||
                materialName == null || materialName.isBlank() ||
                !Double.isFinite(quantity) || quantity <= 0) {
            return null;
        }

        String materialId = UUID.randomUUID().toString();

        String sql = """
                INSERT INTO materials (
                    material_id, project_id,
                    material_name, quantity
                )
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, materialId);
            statement.setString(2, projectId);
            statement.setString(3, materialName.trim());
            statement.setDouble(4, quantity);

            statement.executeUpdate();
            return materialId;

        } catch (SQLException e) {
            System.out.println("Could not add material: " + e.getMessage());
            return null;
        }
    }

    // Updates the quantity of an existing material.
    @Override
    public boolean updateMaterialQuantity(
            String materialId,
            double quantity) {

        if (materialId == null || materialId.isBlank() ||
                !Double.isFinite(quantity) || quantity < 0) {
            return false;
        }

        String sql = """
                UPDATE materials
                SET quantity = ?
                WHERE material_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, quantity);
            statement.setString(2, materialId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Could not update material: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean removeMaterial(String materialId) {

        if (materialId == null || materialId.isBlank()) {
            return false;
        }

        String sql = """
                DELETE FROM materials
                WHERE material_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, materialId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Could not remove material: " + e.getMessage());
            return false;
        }
    }

    // Adds equipment to a project.
    @Override
    public String addEquipment(
            String projectId,
            String equipmentName) {

        if (projectId == null || projectId.isBlank() ||
                equipmentName == null || equipmentName.isBlank()) {
            return null;
        }

        String equipmentId = UUID.randomUUID().toString();

        String sql = """
                INSERT INTO equipment (
                    equipment_id, project_id, equipment_name
                )
                VALUES (?, ?, ?);
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, equipmentId);
            statement.setString(2, projectId);
            statement.setString(3, equipmentName.trim());

            statement.executeUpdate();
            return equipmentId;

        } catch (SQLException e) {
            System.out.println("Could not add equipment: " + e.getMessage());
            return null;
        }
    }

    // Removes equipment from a project.
    @Override
    public boolean removeEquipment(String equipmentId) {

        if (equipmentId == null || equipmentId.isBlank()) {
            return false;
        }

        String sql = """
                DELETE FROM equipment
                WHERE equipment_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, equipmentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Could not remove equipment: " + e.getMessage());
            return false;
        }
    }

    // Retrieves materials and equipment assigned to a project.
    @Override
    public String getProjectMaterialsAndEquipment(String projectId) {

        if (projectId == null || projectId.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        String materialsSql = """
                SELECT material_name, quantity
                FROM materials
                WHERE project_id = ?;
                """;

        String equipmentSql = """
                SELECT equipment_name
                FROM equipment
                WHERE project_id = ?;
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            result.append("Materials:\n");

            try (PreparedStatement statement =
                         connection.prepareStatement(materialsSql)) {

                statement.setString(1, projectId);

                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        result.append(rows.getString("material_name"))
                              .append(" - Quantity: ")
                              .append(rows.getDouble("quantity"))
                              .append("\n");
                    }
                }
            }

            result.append("Equipment:\n");

            try (PreparedStatement statement =
                         connection.prepareStatement(equipmentSql)) {

                statement.setString(1, projectId);

                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        result.append(rows.getString("equipment_name"))
                              .append("\n");
                    }
                }
            }

            return result.toString();

        } catch (SQLException e) {
            System.out.println("Could not retrieve materials and equipment: "
                    + e.getMessage());
            return "";
        }
    }
}

