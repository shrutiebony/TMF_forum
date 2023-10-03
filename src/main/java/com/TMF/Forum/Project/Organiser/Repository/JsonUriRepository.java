package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalRequestStructureDocument;
import com.TMF.Forum.Project.Organiser.Document.JsonUriDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonUriRepository extends MongoRepository<JsonUriDocuments, String> {


}
