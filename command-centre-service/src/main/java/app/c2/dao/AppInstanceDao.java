package app.c2.dao;

import app.c2.model.AppInstance;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface AppInstanceDao extends CrudRepository<AppInstance, String> {

    List<AppInstance> findByProjectId (long projectId);
    List<AppInstance> findByProjectIdAndName (long projectId, String name);

}
