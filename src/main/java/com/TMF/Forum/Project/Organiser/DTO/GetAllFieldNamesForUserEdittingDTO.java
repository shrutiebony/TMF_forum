package com.TMF.Forum.Project.Organiser.DTO;

import com.TMF.Forum.Project.Organiser.DTO.UserJsonInputKeyValuePairDTO;
import lombok.Data;

import java.util.List;

@Data
public class GetAllFieldNamesForUserEdittingDTO {
    public String endPoint;
    public String pathName;
    public String methodType;
    public String identifyingFeature;
    public List<UserJsonInputKeyValuePairDTO> fieldNames;
}
