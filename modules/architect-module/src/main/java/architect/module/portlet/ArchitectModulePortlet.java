package architect.module.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ParamUtil;

import java.io.IOException;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;

import architect.module.constants.ArchitectModulePortletKeys;

/**
 * @author pumba
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=ArchitectModule",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + ArchitectModulePortletKeys.ARCHITECTMODULE,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class ArchitectModulePortlet extends MVCPortlet {

	private Log log = LogFactoryUtil.getLog(ArchitectModulePortlet.class);
	
    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {

        String cmd = ParamUtil.getString(actionRequest, ActionRequest.ACTION_NAME);

        if ("check_tls".equals(cmd)) {
            checkTlsVersion(actionResponse);
        }
                
    }

	private void checkTlsVersion(ActionResponse actionResponse) throws IOException {

		log.info("CheckTLSVersions - JDK TLS Suportados");

		try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket()) {

			String[] protocols = socket.getSupportedProtocols();
			log.info("CheckTLSVersions - Protocolos disponiveis: " + Arrays.toString(protocols));

			String[] enabledProtocols = socket.getEnabledProtocols();
			log.info("CheckTLSVersions - Protocolos habilitados: " + Arrays.toString(enabledProtocols));
			
	        // Passa o resultado como parâmetro render
	        actionResponse.setRenderParameter("protocols", Arrays.toString(protocols));
	        actionResponse.setRenderParameter("enabledProtocols", Arrays.toString(enabledProtocols));

		} catch (Exception e) {
			log.error("CheckTLSVersions - Erro ao verificar protocolos TLS: " + e.getMessage(), e);
		}

	}

}