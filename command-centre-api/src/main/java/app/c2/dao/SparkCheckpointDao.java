package app.c2.dao;

import app.c2.model.SparkCheckpoint;
import app.c2.model.compositeField.SparkCheckpointId;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface SparkCheckpointDao extends CrudRepository<SparkCheckpoint, SparkCheckpointId> {
    List<SparkCheckpoint> findByProjectId (long projectId);
}
