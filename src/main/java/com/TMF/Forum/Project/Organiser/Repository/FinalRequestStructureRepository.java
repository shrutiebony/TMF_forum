package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalRequestStructureDocument;
import com.TMF.Forum.Project.Organiser.Document.PathRelationDefinerDocument_AllRelationsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinalRequestStructureRepository extends MongoRepository<FinalRequestStructureDocument, String> {
    List<FinalRequestStructureDocument> findOneByEndPointAndPathNameAndMethodType(String endPoint, String pathName, String methodType);
}
