package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.dao.AnnouncementDAO;
import be.ugent.degage.db.models.Announcement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link be.ugent.degage.db.dao.AnnouncementDAO}
 */
class JDBCAnnouncementDAO extends AbstractDAO implements AnnouncementDAO {

    public JDBCAnnouncementDAO(JDBCDataAccessContext context) {
        super(context);
    }

    private static final String ANNOUNCEMENT_FIELDS =
            "announcement_key, announcement_description, announcement_html ";

    private static final String ANNOUNCEMENT_FIELDS_FULL =
            ANNOUNCEMENT_FIELDS + ", announcement_md ";

    private static Announcement populateAnnouncement(ResultSet rs) throws SQLException {
        return new Announcement(
                rs.getString("announcement_key"),
                rs.getString("announcement_description"),
                rs.getString("announcement_html"),
                null
        );
    }


    private static Announcement populateAnnouncementFull(ResultSet rs) throws SQLException {
        return new Announcement(
                rs.getString("announcement_key"),
                rs.getString("announcement_description"),
                rs.getString("announcement_html"),
                rs.getString("announcement_md")
        );
    }

    @Override
    public Announcement getAnnouncement(String key) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + ANNOUNCEMENT_FIELDS + "FROM announcements WHERE announcement_key = ?"
        )) {
            ps.setString(1, key);
            return toSingleObject(ps, JDBCAnnouncementDAO::populateAnnouncement);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get announcement", ex);
        }
    }

    @Override
    public Announcement getAnnouncementFull(String key) {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + ANNOUNCEMENT_FIELDS_FULL + "FROM announcements WHERE announcement_key = ?"
        )) {
            ps.setString(1, key);
            return toSingleObject(ps, JDBCAnnouncementDAO::populateAnnouncementFull);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get announcement", ex);
        }
    }

    @Override
    public void updateAnnouncement(String key, String html, String markdown) {
        try (PreparedStatement ps = prepareStatement(
                "UPDATE announcements SET announcement_html = ?, announcement_md = ? WHERE key = ?"
        )) {
            ps.setString(1,html);
            ps.setString(2,markdown);
            ps.setString(3,key);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update announcement", ex);
        }
    }

    @Override
    public Iterable<Announcement> listAnnouncements() {
        try (PreparedStatement ps = prepareStatement(
                "SELECT " + ANNOUNCEMENT_FIELDS + "FROM announcements ORDER BY announcement_key"
        )) {
            return toList(ps, JDBCAnnouncementDAO::populateAnnouncement);
        } catch (SQLException ex) {
            throw new DataAccessException("Cannot get announcement", ex);
        }
    }
}
