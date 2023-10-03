package com.TMF.Forum.Project.Organiser.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class ResponseSchemaDTO {
    public String description;
    public SchemaDTO schema;
    public String statusCode;
}
