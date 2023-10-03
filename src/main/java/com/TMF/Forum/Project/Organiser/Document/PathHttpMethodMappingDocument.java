package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@Document(collection = "pathHttpMethodMapping")

public class PathHttpMethodMappingDocument {
    public String endPoint;
    public String pathName;
    public List<String> allMethodTypes;

}
