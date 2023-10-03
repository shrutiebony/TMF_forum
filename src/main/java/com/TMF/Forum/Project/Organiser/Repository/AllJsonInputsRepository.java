package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.AllJsonInputsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AllJsonInputsRepository extends MongoRepository<AllJsonInputsDocument, String> {

}
