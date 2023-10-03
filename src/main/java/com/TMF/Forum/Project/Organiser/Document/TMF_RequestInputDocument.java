package com.TMF.Forum.Project.Organiser.Document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "UserChosenTMFParameters")
public class TMF_RequestInputDocument {
        public String endPoint;
        public String pathName;
        public String methodType;
        public String responseType;
}
