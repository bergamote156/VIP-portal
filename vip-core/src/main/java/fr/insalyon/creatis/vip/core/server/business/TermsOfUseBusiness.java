package fr.insalyon.creatis.vip.core.server.business;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.TermsOfUse;
import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.server.dao.TermsUseDAO;

@Service
public class TermsOfUseBusiness extends CommonBusiness {
    
    private final TermsUseDAO termsUseDAO;

    @Autowired
    public TermsOfUseBusiness(TermsUseDAO termsUseDAO) {
        this.termsUseDAO = termsUseDAO;
    }

    public void add() throws VipException {
        try {
            TermsOfUse termsOfUse = new TermsOfUse(new Timestamp(System.currentTimeMillis()));
            termsUseDAO.add(termsOfUse);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public Timestamp getLastUpdate() throws VipException {
        try {
            return termsUseDAO.getLastUpdateTermsOfUse();
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }
}
