package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.AllUserInputsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllUserInputRepository extends MongoRepository<AllUserInputsDocument, String> {
    AllUserInputsDocument findByIdentifyingFeature(String feature);
    List<AllUserInputsDocument> findAllByTableName(String tableName);


}
