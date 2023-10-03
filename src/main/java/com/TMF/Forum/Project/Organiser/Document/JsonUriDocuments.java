package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "JsonUriDocument")
public class JsonUriDocuments {
    private String jsonUri;
}
