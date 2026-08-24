package fr.insalyon.creatis.vip.publication.server.rpc;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.client.view.CoreException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.rpc.AbstractRemoteServiceServlet;
import fr.insalyon.creatis.vip.publication.client.rpc.PublicationService;
import fr.insalyon.creatis.vip.publication.models.Publication;
import fr.insalyon.creatis.vip.publication.models.PublicationType;
import fr.insalyon.creatis.vip.publication.server.business.PublicationBusiness;
import jakarta.servlet.ServletException;

public class PublicationServiceImpl extends AbstractRemoteServiceServlet implements PublicationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private PublicationBusiness publicationBusiness;

    public PublicationServiceImpl() {
        logger.info("PublicationServiceImpl: Creating PublicationBusiness.");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        publicationBusiness = getBean(PublicationBusiness.class);
    }

    @Override
    public List<Publication> getPublications() throws CoreException {
        trace(logger, "Getting publication list.");
        try {
            return publicationBusiness.getPublications();
        } catch (VipException ex) {
            throw new CoreException(ex);
        }
    }

    @Override
    public void removePublication(Long id) throws CoreException {
        trace(logger, "Removing publication.");

        try {
            publicationBusiness.removePublication(id);
        } catch (VipException ex) {
            throw new CoreException(ex);
        }
    }

    @Override
    public void addPublication(Publication pub) throws CoreException {
        trace(logger, "Adding publication.");

        try {
            User user = getSessionUser();
            publicationBusiness.addPublication(pub);
        } catch (VipException ex) {
            throw new CoreException(ex);
        }
    }

    @Override
    public void updatePublication(Publication pub) throws CoreException {
        trace(logger, "Updating publication.");

        try {
            publicationBusiness.updatePublication(pub);
        } catch (VipException ex) {
            throw new CoreException(ex);
        }
    }

    @Override
    public List<Publication> parseBibtexText(String s) throws CoreException {
        try {
            User user = getSessionUser();
            return publicationBusiness.parseBibtexText(s, user == null ? null : user.getEmail());
        } catch (VipException ex) {
            throw new CoreException(ex);
        }
    }


}
