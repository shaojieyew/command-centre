package app.c2.dao;

import app.c2.model.NifiQuery;
import app.c2.model.compositeField.NifiQueryId;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface NifiQueryDao extends CrudRepository<NifiQuery, NifiQueryId> {

    List<NifiQuery> findByProjectId (long projectId);
    List<NifiQuery> findByProjectIdAndName (long projectId, String userId);

}
