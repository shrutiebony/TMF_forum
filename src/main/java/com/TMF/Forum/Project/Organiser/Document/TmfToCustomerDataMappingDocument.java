package com.TMF.Forum.Project.Organiser.Document;

import com.TMF.Forum.Project.Organiser.DTO.CustomerTableDetailsMappingWithTMF;
import com.TMF.Forum.Project.Organiser.DTO.DefaultValuesDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "TmfToCustomerDataMapping")
public class TmfToCustomerDataMappingDocument {
    @Id
    private String id;
    private String endPoint;
    public String methodType;
    public String pathName;
    public List<CustomerTableDetailsMappingWithTMF> mapping;
    public List<DefaultValuesDTO> defaults;
  }

