package tn.esprit.clientmanagmentctinetwork.Dto;

public class VisitScheduleDto {
    private String date; // YYYY-MM-DD
    private String filePath;
    private String fileName;

    public VisitScheduleDto() {}

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}