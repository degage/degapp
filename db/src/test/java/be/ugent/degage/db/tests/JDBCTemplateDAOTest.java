package be.ugent.degage.db.tests;

import be.ugent.degage.db.*;
import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessProvider;
import be.ugent.degage.db.dao.TemplateDAO;
import be.ugent.degage.db.jdbc.JDBCDataAccess;
import be.ugent.degage.db.jdbc.JDBCFilter;
import be.ugent.degage.db.models.EmailTemplate;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class JDBCTemplateDAOTest {

    private DataAccessContext context;

    private TemplateDAO templateDAO;
	private List<EmailTemplate> templates;

    /**
     * Initializes the DAOs so they can be used in the test methods.
     * @throws Exception
     */
	@Before
    public void setUp() throws Exception {
        DataAccessProvider provider = JDBCDataAccess.getTestDataAccessProvider();
        context = provider.getDataAccessContext();

        templateDAO = context.getTemplateDAO();
        templates = templateDAO.getTemplateList(FilterField.TEMPLATE_NAME, true, 1, 100, new JDBCFilter());
    }

    /**
     * Tests getting and updating (not creating!) of templates
     * @throws Exception
     */
	//@Test
	public void testTemplateDAO() throws Exception {
        try {
		    getTemplateTest();
		    updateTemplateTest();
        } finally {
            context.rollback();
        }
	}
	
	private void updateTemplateTest() throws Exception{
		Iterator<EmailTemplate> it = templates.iterator();
		Scanner sc = new Scanner(JDBCTemplateDAOTest.class.getResourceAsStream("/be/ugent/degage/db/tests/random/random_text.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext() && it.hasNext()) {
            String body = sc.next();
            EmailTemplate template = (EmailTemplate) it.next();
            String templateSubject = "";
            boolean templateSendMail = false;

            templateDAO.updateTemplate(template.getId(), body, templateSubject, templateSendMail);
            EmailTemplate template2 = templateDAO.getTemplate(template.getId());
            
            assertEquals(template2.getBody(), body);
            assertFalse(template.getBody().equals(template2.getBody()));
        }
        sc.close();
	}
	
	private void getTemplateTest(){
		for(EmailTemplate template : templates){
			int id = template.getId();
			String body = template.getBody();
			String title = template.getTitle();
			List<String> tags = template.getUsableTags();
			
			EmailTemplate template2 = templateDAO.getTemplate(id);
			assertEquals(id, template2.getId());
			assertEquals(body,template2.getBody());
			assertEquals(title,template2.getTitle());
			assertTrue(tags.containsAll(template2.getUsableTags()));
			assertTrue(template2.getUsableTags().containsAll(tags));
		}
	}
}
