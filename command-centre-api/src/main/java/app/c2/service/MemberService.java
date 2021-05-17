package app.c2.service;

import app.c2.dao.MemberDao;
import app.c2.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

  @Autowired private MemberDao memberDao;

  public List<Member> findByProjectId(long projectId) {
    return memberDao.findByProjectId(projectId);
  }
  public List<Member> findByUserId(String userId) {
    return memberDao.findByUserId(userId);
  }

}
