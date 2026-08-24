package fr.insalyon.creatis.vip.core.server.business;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.business.proxy.ProxyClient;

// this class ensure that the proxy is renewed every each 10 hours
// the schedule of the task is handled by spring
@Component
public class ProxyBusiness extends CommonBusiness {

    private final Server server;
    private final ProxyClient proxyClient;
    private final EmailBusiness emailBusiness;

    @Autowired
    public ProxyBusiness(ProxyClient proxyClient, Server server, EmailBusiness emailBusiness) {
        this.server = server;
        this.proxyClient = proxyClient;
        this.emailBusiness = emailBusiness;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.HOURS)
    public void configure() {
        if (server.getMyProxyEnabled()) {
            try {
                logger.debug("Configuring VIP server proxy.");
                proxyClient.checkProxy();

            } catch (Exception ex) {
                logger.error("Error configuring myproxy : {}", ex.getMessage());
                emailBusiness.sendErrorEmailToAdmins("Error while configuring dirac proxy!", ex, "internal system (scheduled by spring)");
            }
        } else {
            logger.info("Proxy not needed and not validated !");
        }
    }
}
