package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.PathRelationDefinerDocument_AllRelationsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathRelationDefinerDocument_AllRelationsRepository extends MongoRepository<PathRelationDefinerDocument_AllRelationsDocument, String> {
List<PathRelationDefinerDocument_AllRelationsDocument> findAll();
}
