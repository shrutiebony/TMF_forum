package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalResponseStructureDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinalResponseStructureRepository extends MongoRepository<FinalResponseStructureDocument, String> {
   List<FinalResponseStructureDocument> findOneByEndPointAndPathNameAndMethodType(String endPoint, String pathName, String methodType);
}
