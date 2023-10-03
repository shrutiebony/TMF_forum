package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.PathHttpMethodMappingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathHttpMethodMappedRepository extends MongoRepository<PathHttpMethodMappingDocument, String> {
    List<PathHttpMethodMappingDocument> findOneByEndPointAndPathName(String endPoint, String pathName);
    List<PathHttpMethodMappingDocument> findOneByEndPoint(String endPoint);

}
