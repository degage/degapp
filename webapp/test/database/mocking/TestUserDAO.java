package database.mocking;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.UserDAO;
import be.ugent.degage.db.models.User;
import be.ugent.degage.db.models.UserStatus;
import be.ugent.degage.db.models.VerificationType;
import utility.Cloner;

import java.util.*;

/**
 * Created by Cedric on 3/7/14.
 */
public class TestUserDAO implements UserDAO {

    private class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K k, V v) {
            this.key = k;
            this.value = v;
        }

        public void setKey(K k) {
            this.key = k;
        }

        public void setValue(V v) {
            this.value = v;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    private int userIdCounter;
    private List<User> userList;
    private EnumMap<VerificationType, Map<Integer, String>> verifications;

    public TestUserDAO() {
        this.userList = new ArrayList<User>();
        this.userIdCounter = 0;
        this.verifications = new EnumMap<>(VerificationType.class);
    }

    @Override
    public User getUser(String email) throws DataAccessException {
        for (User user : userList) {
            if (user.getEmail().equals(email))
                return (User) Cloner.clone(user);
        }
        return null;
    }

    @Override
    public User getUser(int userId, boolean withRest) throws DataAccessException {
        for (User user : userList) {
            if (user.getId() == userId)
                return (User)Cloner.clone(user);
        }
        return null;
    }

    @Override
    public void updateUser(User user, boolean withRest) throws DataAccessException {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == user.getId()) {
                userList.set(i, user);
                return;
            }
        }
    }

    @Override
    public void deleteUser(User user) throws DataAccessException {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == user.getId()) {
                userList.get(i).setStatus(UserStatus.DROPPED);
                return;
            }
        }
    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName) throws DataAccessException {
        User user = new User(userIdCounter++, email, firstName, lastName, password);
        userList.add(user);
        return user;
    }

    @Override
    public String getVerificationString(User user, VerificationType type) throws DataAccessException {
        Map<Integer, String> keyMap = verifications.get(type);
        if (keyMap == null)
            return null;
        else
            return keyMap.get(user.getId());
    }

    @Override
    public String createVerificationString(User user, VerificationType type) throws DataAccessException {
        Map<Integer, String> keyMap = verifications.get(type);
        if (keyMap == null) {
            keyMap = verifications.put(type, new HashMap<Integer, String>());
        }
        keyMap.put(user.getId(), UUID.randomUUID().toString());
        return null;
    }

    @Override
    public void deleteVerificationString(User user, VerificationType type) throws DataAccessException {
        Map<Integer, String> keyMap = verifications.get(type);
        if (keyMap == null) {
            throw new RuntimeException("Verification map did not exist.");
        } else {
            keyMap.remove(user.getId());
        }
    }

    public List<User> getAllUsers() throws DataAccessException {
        return userList;
    }

	@Override
	public int getAmountOfUsers(Filter filter) throws DataAccessException {
		return getAllUsers().size(); // TODO: add filter methods
	}

	@Override
	public List<User> getUserList(FilterField orderBy, boolean asc, int page,
			int pageSize, Filter filter) throws DataAccessException {
		return getAllUsers(); // TODO: add filter methods
	}
}
