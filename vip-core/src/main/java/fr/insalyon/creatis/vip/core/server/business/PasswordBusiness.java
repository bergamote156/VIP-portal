package fr.insalyon.creatis.vip.core.server.business;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insalyon.creatis.devtools.MD5;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.server.dao.UserDAO;

@Service
public class PasswordBusiness extends CommonBusiness {
    
    private final UserDAO userDAO;

    @Autowired
    public PasswordBusiness(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void update(User user, String currentPassword, String newPassword) throws VipException {
        try {
            currentPassword = MD5.get(currentPassword);
            newPassword = MD5.get(newPassword);

            userDAO.updatePassword(user.getEmail(), currentPassword, newPassword);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.error("Error updating password for {}", user.getEmail(), ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void setPassword(String email, String newPassword) throws VipException {
        try {
            userDAO.resetPassword(email, MD5.get(newPassword));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.error("Error setting password for {}", email, ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }


    public void reset(String email, String code, String password) throws VipException {
        try {
            User user = userDAO.get(email);

            if (code.equals(user.getCode())) {
                userDAO.resetPassword(email, MD5.get(password));
            } else {
                logger.error("Wrong reset code for {} : {}", email, code);
                throw new VipException("Wrong reset code.");
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.error("Error resetting password for {}", email, ex);
            throw new VipException(ex);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }
}
