package com.TMF.Forum.Project.Organiser.Document;

import com.TMF.Forum.Project.Organiser.DTO.UserJsonInputKeyValuePairDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "AllUserInputs")
public class AllUserInputsDocument {
    @Id
    private String id;
    private List<UserJsonInputKeyValuePairDTO> userData;
    private String identifyingFeature;
    private String pathName;
    private String methodType;
    private String responseStatus;
    private String tableName;
}
