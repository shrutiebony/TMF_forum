package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "databaseSearchCriteria")
public class DatabaseSearchCriteriaDocument {
    @Id
    private String id;
    private String tableName;
    private String fieldName;
    private String searchCriteria;

}
