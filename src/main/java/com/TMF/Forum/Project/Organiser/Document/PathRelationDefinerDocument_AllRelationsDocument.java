package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@Document(collection = "tmfJsonStructure_Evaluated")
public class PathRelationDefinerDocument_AllRelationsDocument {
    public String endPoint;
    public List<PathHttpMethodMappingDocument> pathMethodTypeMapping;
    public List<FinalRequestStructureDocument> allJsonStructures;
    public List<FinalResponseStructureDocument> allResponseStructure;
}
