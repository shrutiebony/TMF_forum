package com.TMF.Forum.Project.Organiser.Document;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
@Data
@Document(collection = "AllJsonInputs")
public class AllJsonInputsDocument {
    @Id
    private String id;
    private Map<String, JsonNode > data;
    private String identifyingFeature;
}
