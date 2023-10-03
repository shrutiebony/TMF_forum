package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalUserJsonOutputDocument;
import com.TMF.Forum.Project.Organiser.Document.TableMappingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CellDataRepository extends MongoRepository<TableMappingDocument, String> {


}
