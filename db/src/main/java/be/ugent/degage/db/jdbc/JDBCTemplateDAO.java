package be.ugent.degage.db.jdbc;

import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.models.EmailTemplate;
import be.ugent.degage.db.models.MailType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
class JDBCTemplateDAO extends AbstractDAO implements TemplateDAO {

    private String TEMPLATE_QUERY = "SELECT template_id, template_title, template_body, template_subject, template_send_mail, template_send_mail_changeable " +
            "FROM templates ";

    private String FILTER_FRAGMENT = " WHERE template_title LIKE ? ";

    private void fillFragment(PreparedStatement ps, Filter filter, int start) throws SQLException {
        if(filter == null) {
            // getFieldContains on a "empty" filter will return the default string "%%", so this does not filter anything
            filter = new JDBCFilter();
        }
        ps.setString(start, filter.getValue(FilterField.TEMPLATE_NAME));
    }

    public JDBCTemplateDAO(JDBCDataAccessContext context) {
        super (context);
    }

    private LazyStatement getTemplateStatement = new LazyStatement (
            "SELECT template_id, template_title, template_subject, template_body, template_send_mail, template_send_mail_changeable " +
                    "FROM templates WHERE template_id = ?;"
    );

    private LazyStatement getAmountOfTemplatesStatement = new LazyStatement (
            "SELECT count(template_id) as amount_of_templates FROM templates " + FILTER_FRAGMENT
    );

    private LazyStatement getTemplateListPageByTitleAscStatement = new LazyStatement (
            TEMPLATE_QUERY + FILTER_FRAGMENT + " ORDER BY template_title asc LIMIT ?, ?"
    );
    private LazyStatement getTemplateListPageByTitleDescStatement = new LazyStatement (
            TEMPLATE_QUERY + FILTER_FRAGMENT + " ORDER BY template_title desc LIMIT ?, ?"
    );

    private LazyStatement getUpdateTemplateStatement = new LazyStatement (
            "UPDATE templates SET template_subject = ?, template_body = ?, template_send_mail = ? WHERE template_id = ?"
    );

    public EmailTemplate populateEmailTemplate(ResultSet rs) throws SQLException {
        return new EmailTemplate(
                rs.getInt("template_id"), rs.getString("template_title"), rs.getString("template_body"),
                getUsableTags(rs.getInt("template_id")), rs.getString("template_subject"),
                rs.getBoolean("template_send_mail"), rs.getBoolean("template_send_mail_changeable")
        );
    }

    private LazyStatement getTagsByTemplateIdStatement = new LazyStatement (
            "SELECT template_tag_body " +
                    "FROM templatetagassociations JOIN templatetags ON templatetagassociations.template_tag_id = templatetags.template_tag_id"
                    + " WHERE template_id = ?"
    );

    private List<String> getUsableTags(int templateId) throws DataAccessException {
        try {
            PreparedStatement ps = getTagsByTemplateIdStatement.value();
            ps.setInt(1,templateId);
            try (ResultSet rs = ps.executeQuery()) {
                return populateTagList(rs);
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading emailtags resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch templatetags by templateid.", ex);
        }

    }

    public static List<String> populateTagList(ResultSet rs) throws SQLException {
        List<String> usableTags = new ArrayList<>();
        while (rs.next()) {
            usableTags.add(rs.getString("template_tag_body"));
        }
        return usableTags;
    }


    @Override
    public EmailTemplate getTemplate(int templateID) throws DataAccessException {
        try {
            PreparedStatement ps = getTemplateStatement.value();
            ps.setInt(1,templateID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateEmailTemplate(rs);
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading emailtemplate resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch emailtemplate by id.", ex);
        }
    }

    @Override
    public EmailTemplate getTemplate(MailType type) throws DataAccessException {
        return getTemplate(type.getKey());
    }

    @Override
    public int getAmountOfTemplates(Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = getAmountOfTemplatesStatement.value();
            fillFragment(ps, filter, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next())
                    return rs.getInt("amount_of_templates");
                else return 0;

            } catch (SQLException ex) {
                throw new DataAccessException("Error reading count of templates", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not get count of templates", ex);
        }
    }

    @Override
    public List<EmailTemplate> getTemplateList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException {
        try {
            PreparedStatement ps = null;
            switch(orderBy) {
                default: // TEMPLATE_NAME
                    ps = asc ? getTemplateListPageByTitleAscStatement.value() : getTemplateListPageByTitleDescStatement.value();
                    break;
            }
            if(ps == null) {
                throw new DataAccessException("Could not create getTemplateList statement");
            }

            fillFragment(ps, filter, 1);
            int first = (page-1)*pageSize;
            ps.setInt(2, first);
            ps.setInt(3, pageSize);
            return getTemplates(ps);
        } catch (SQLException ex) {
            throw new DataAccessException("Could not retrieve a list of templates", ex);
        }
    }

    @Override
    public void updateTemplate(int templateID, String templateBody, String templateSubject, boolean templateSendMail) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateTemplateStatement.value();
            ps.setString(1, templateSubject);
            ps.setString(2, templateBody);
            ps.setBoolean(3, templateSendMail);
            ps.setInt(4,templateID);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating template.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private List<EmailTemplate> getTemplates(PreparedStatement ps) {
        List<EmailTemplate> templates = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                templates.add(populateEmailTemplate(rs));
            }
            return templates;
        } catch (SQLException ex) {
            throw new DataAccessException("Error reading templates resultset", ex);
        }
    }
}
