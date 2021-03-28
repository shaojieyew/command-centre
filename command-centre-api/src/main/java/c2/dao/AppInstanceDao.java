package c2.dao;

import c2.model.AppInstance;
import c2.model.File;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface AppInstanceDao extends CrudRepository<AppInstance, String> {

    List<AppInstance> findByProjectId (long projectId);
    List<AppInstance> findByProjectAppId (String projectAppId);

}
