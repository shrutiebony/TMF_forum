package com.TMF.Forum.Project.Organiser.Document;

import com.TMF.Forum.Project.Organiser.DTO.ResponseSchemaDTO;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "finalResponseStructureDocument")
public class FinalResponseStructureDocument {
    public String endPoint;
    public String operationId;
    public ArrayList<String> tags;
    public List<ResponseSchemaDTO> responseSchemaDTOSList;
    public String methodType;
    public String pathName;
}
