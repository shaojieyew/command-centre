package c2.dao;

import c2.model.File;
import c2.model.Member;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface FileDao extends CrudRepository<File, Long> {

    List<File> findByProjectId (long projectId);
    List<File> findByFileHash (String hash);
}
