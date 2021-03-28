package c2.dao;

import c2.model.App;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface AppDao extends CrudRepository<App, String> {}
