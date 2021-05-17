package app.c2.service;

import app.c2.dao.UserDao;
import app.c2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

  @Autowired private UserDao userDao;

  public List<User> findAll() {
    return (List<User>) userDao.findAll();
  }
  public Optional<User> findById(String userId) {
    return userDao.findById(userId);
  }

  public User save(User user) {
    return userDao.save(user);
  }

  public void delete(User user) {
    userDao.delete(user);
  }
}
