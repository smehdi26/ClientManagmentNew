package tn.esprit.clientmanagmentctinetwork.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for validating a single maintenance visit slot.
 */
public class VisitValidationDto {

    @NotNull(message = "Visit index is required")
    @Min(value = 1, message = "Index must be at least 1")
    @Max(value = 6, message = "Index cannot exceed 6")
    private Integer visitIndex;

    @NotBlank(message = "Visit date is required")
    private String date; // Expected format YYYY-MM-DD

    private String observations;

    private String filePath;

    private String fileName;

    public VisitValidationDto() {}

    // Getters and Setters
    public Integer getVisitIndex() { return visitIndex; }
    public void setVisitIndex(Integer visitIndex) { this.visitIndex = visitIndex; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}