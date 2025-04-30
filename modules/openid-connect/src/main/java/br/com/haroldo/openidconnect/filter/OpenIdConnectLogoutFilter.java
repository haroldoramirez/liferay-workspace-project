package br.com.haroldo.openidconnect.filter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

import br.com.haroldo.openidconnect.utils.OpenIdConnectFilterUtil;

@Component(
    immediate = true,
    property = {
        "servlet-context-name=",
        "servlet-filter-name=OpenIdConnect Logout Filter",
        "url-pattern=/c/portal/logout"
    },
    service = Filter.class
)
public class OpenIdConnectLogoutFilter implements Filter {
	
	private Log log = LogFactoryUtil.getLog(OpenIdConnectLogoutFilter.class);
	
    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
	
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    	
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnect Logout Filter.");
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		String currentURL = OpenIdConnectFilterUtil.getRedirectParameter(request);
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnect Logout Filter - currentURL: " + currentURL);
		}
		
		//Executar uma logica para verifica de tipo de logout

        // Redireciona para o logout do RHSSO (Keycloak, por exemplo)
        String idpLogoutUrl = "https://seu-idp.com/auth/realms/SEU_REALM/protocol/openid-connect/logout";
        String redirectUrl = "https://seu-liferay.com"; // apos logout do IdP

        String finalLogoutUrl = idpLogoutUrl + "?redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8");
        
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnect Logout Filter - redirecionando para a URL: " + finalLogoutUrl);
		}

		response.sendRedirect(finalLogoutUrl);
        
    }

}