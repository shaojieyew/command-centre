package app.c2.dao;

import app.c2.model.App;
import app.c2.model.compositeField.AppId;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface AppDao extends CrudRepository<App, AppId> {
    List<App> findByProjectId (long projectId);
}
