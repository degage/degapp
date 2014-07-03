package database.mocking;

import java.util.ArrayList;
import java.util.List;

import be.ugent.degage.db.models.*;

import org.joda.time.DateTime;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.InfoSessionDAO;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.jdbc.JDBCFilter;

public class TestInfoSessionDAO implements InfoSessionDAO {
	
	private List<InfoSession> sessions;
	private int idCounter;
	
	
	public TestInfoSessionDAO(){
		sessions = new ArrayList<>();
		idCounter=0;
	}

	@Override
	public InfoSession getInfoSession(int id, boolean withAttendees) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==id){
				InfoSession newSession = new InfoSession(session.getId(), session.getType(), session.getTime(), session.getAddress(), session.getHost(), session.getMaxEnrollees(), session.getComments());
				if(withAttendees){
					List<Enrollee> enrolled = new ArrayList<>();
					for(Enrollee enrollee : session.getEnrolled()){
						enrolled.add(enrollee);
					}
					newSession.setEnrolled(enrolled);
				}
				return session;
			}
		}
		return null;
	}

    @Override
    public int getAmountOfAttendees(int infosessionId) throws DataAccessException {
        return 0;
    }

    @Override
	public boolean deleteInfoSession(int id) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==id){
				sessions.remove(session);
				return true;
			}
		}
		return false;
	}

	@Override
	public void registerUser(InfoSession session, User user) throws DataAccessException {
		session.addEnrollee(new Enrollee(user, EnrollementStatus.ENROLLED));
	}

	@Override
	public void setUserEnrollmentStatus(InfoSession session, User user, EnrollementStatus status) throws DataAccessException {
		session.addEnrollee(new Enrollee(user, status));
		
	}

	@Override
	public void unregisterUser(InfoSession session, User user)	throws DataAccessException {
		for(Enrollee enrollee : session.getEnrolled()){
			if(enrollee.getUser().getId()==user.getId()){
				session.deleteEnrollee(enrollee);
				return;
			}
		}		
	}

	@Override
	public void unregisterUser(int infoSessionId, int userId) throws DataAccessException {
		for(InfoSession session : sessions){
			if(session.getId()==infoSessionId){
				for(Enrollee enrollee : session.getEnrolled()){
					if(enrollee.getUser().getId()==userId){
						session.deleteEnrollee(enrollee);
						return;
					}
				}
			}
		}
	}

	@Override
	public InfoSession getAttendingInfoSession(User user) throws DataAccessException {
		for(InfoSession session : sessions){
			for(Enrollee enrollee : session.getEnrolled()){
				if(enrollee.getUser().getId()==user.getId()){
					return session;
				}
			}
		}
		return null;
	}

    @Override
    public void updateInfoSession(InfoSession session) throws DataAccessException {

    }

    @Override
    public Tuple<InfoSession, EnrollementStatus> getLastInfoSession(User user) throws DataAccessException {
        return null;
    }

    @Override
	public Filter createInfoSessionFilter() {
		return new JDBCFilter();
	}

	@Override
	public InfoSession createInfoSession(InfoSessionType type, String typeAlternative, User host,
			Address address, DateTime time, int maxEnrollees, String commentaar)
			throws DataAccessException {
		InfoSession session = new InfoSession(idCounter++, type, time, address, host, maxEnrollees, commentaar);
		sessions.add(session);
		return session;
	}

	@Override
	public int getAmountOfInfoSessions(Filter filter) throws DataAccessException {
		return 0; // TODO: implement filter methods
	}

    @Override
    public List<InfoSession> getInfoSessions(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        return null;
    }

}
