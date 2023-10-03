package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.DatabaseSearchCriteriaDocument;
import com.TMF.Forum.Project.Organiser.Document.JsonUriDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSearchCriteriaRepository extends MongoRepository<DatabaseSearchCriteriaDocument, String> {


}
