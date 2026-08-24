package fr.insalyon.creatis.vip.application.client.view.monitor;

import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import java.util.Date;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public interface MonitorParserInterface {

    public boolean parse(String applicationName);

    public Layout.TabFactoryAndId getTab(
            String simulationId, String simulationName,
            WorkflowStatus status, Date launchedDate);
}
