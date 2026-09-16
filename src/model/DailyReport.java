package model;

public class DailyReport {

    private String reportId;
    private String projectId;
    private String report;

    public DailyReport(String reportId, String projectId, String report) {
        this.reportId = reportId;
        this.projectId = projectId;
        this.report = report;
    }

//  get Method
    public String getReportId() {
        return reportId;
    }
    public String getProjectId() {
        return projectId;
    }
    public String getReport() {
        return report;
    }
    
//  set Method
    public void setReport(String report) {
        this.report = report;
    }
}
