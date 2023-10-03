package com.TMF.Forum.Project.Organiser.DTO;

import lombok.Data;

@Data
public class ParameterDTO {
    public String name;
    public String description;
    public boolean required;
    public String in;
    public String type;
    public SchemaDTO schema;
}
