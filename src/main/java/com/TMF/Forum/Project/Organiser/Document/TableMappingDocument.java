package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "TableMapping")

public class TableMappingDocument {
    @Id
    private String id;
    private int rowNumber;
    private int columnNumber;
    private String fieldValue;
}