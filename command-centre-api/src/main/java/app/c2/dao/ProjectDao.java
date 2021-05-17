package app.c2.dao;

import app.c2.model.Project;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface ProjectDao extends CrudRepository<Project, Long> {

    List<Project> findByOwner (String owner);

    Optional<Project> findById (Long id);
}
