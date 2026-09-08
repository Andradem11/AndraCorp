package applicationapi;
/*
 * This interface is responsible for:
 * Contractors will mainly create and modify projects.
 * Clients will mainly retrieve project information.
 */
public interface ProjectManagementAPI {
//	Creates a new construction project for a contractor.
	String createProject(
			String contractorId,
			String projectName);

//	Connects a client account to a specific project.
	boolean addClientToProject(
			String porjectId,
			String cleintId);
	
//	Updates general project information.
	boolean updateProject(String projectId);
	
//	Removes a project from the system.
	boolean deleteProject(String projectId);
	
//	Retrieves information about a specific project.
	String getProject(String projectId);
}
