package app.c2.dao;

import app.c2.model.Member;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface MemberDao extends CrudRepository<Member, Long> {

    List<Member> findByProjectId (long projectId);
    List<Member> findByUserId (String userId);

}
