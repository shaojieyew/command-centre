package app.c2.dao;

import app.c2.model.File;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface FileDao extends CrudRepository<File, Long> {

    List<File> findByProjectId (long projectId);
    List<File> findByFileHash (String hash);
}
