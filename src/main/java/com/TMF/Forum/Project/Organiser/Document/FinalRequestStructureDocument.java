package com.TMF.Forum.Project.Organiser.Document;

import com.TMF.Forum.Project.Organiser.DTO.ParameterDTO;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
@Data
@Document(collection = "finalRequestStructureDocument")
public class FinalRequestStructureDocument {
    public String endPoint;
    public String operationId;
    public ArrayList<String> tags;
    public ArrayList<ParameterDTO> parameters;
    public String methodType;
    public String pathName;
}
