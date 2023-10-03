package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalRequestStructureDocument;
import com.TMF.Forum.Project.Organiser.Document.FinalUserJsonOutputDocument;
import com.TMF.Forum.Project.Organiser.Document.TmfToCustomerDataMappingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TmfToCustomerDataMappingRepository extends MongoRepository<TmfToCustomerDataMappingDocument, String> {
    TmfToCustomerDataMappingDocument findOneByEndPointAndPathNameAndMethodType(String endPoint, String pathName, String methodType);

}
