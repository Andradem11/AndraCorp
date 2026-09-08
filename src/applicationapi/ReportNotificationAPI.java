package applicationapi;
/*
 * Handles daily construction reports
 * and system notifications.
 */
public interface ReportNotificationAPI {
//	Creates a daily report for a project.
	String submitDailyReport(
            String projectId,
            String report
    );

//	Retrieves a daily report.
	String getDailyReport(String reportId);

//	Sends a notification to a user.
	boolean sendNotification(
            String userId,
            String message);
	
//	Retrieves notifications belonging to a user.
	String getNotifications(String userId);
}
