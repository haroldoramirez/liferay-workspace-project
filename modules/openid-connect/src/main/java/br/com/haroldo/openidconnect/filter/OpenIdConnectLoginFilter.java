package br.com.haroldo.openidconnect.filter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProviderRegistry;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceHandler;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import br.com.haroldo.openidconnect.utils.OpenIdConnectFilterUtil;

/**
 * @author Haroldo.Nobrega
 */
@Component(
	immediate = true, 
	service = Filter.class,
	property = { 
		"servlet-context-name=", 
		"servlet-filter-name=OpenIdConnect Login Filter",
		"url-pattern=/c/portal/login", 
		"service.ranking:Integer=100" // Alta prioridade para interceptar o servico original
	}
)
public class OpenIdConnectLoginFilter implements Filter {
		
	@Reference
	private OpenIdConnectProviderRegistry<?, ?> openIdConnectProviderRegistry;
	
	@Reference
	private OpenIdConnectServiceHandler openIdConnectServiceHandler;
	
	@Reference
	private Portal portal;
	
	private Log log = LogFactoryUtil.getLog(OpenIdConnectLoginFilter.class);
	private static final String ERRO_OPENID_FILTER = "Erro ao processar o OpenID Connect Login Filter: ";
	private static final String PORTAL_LOGIN_URL = "/c/portal/login";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//Filter implements init()
	}
	
	@Override
	public void destroy() {
		//Filter implements destroy()
	}

	/**
	 * Funcao responsavel por interceptar o processo de filter - redirecionamento de urls caso o usuario nao esteja autenticado
	 * para atender os requisitos do cliente
	 * 
	 * @param servletRequest, servletResponse, chain
	 * @return void
	 * @throws IOException, ServletException Caso ocorra algum erro.
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		try {
			
			// Recupera os nomes dos provedores OpenID Connect
			Collection<String> providerNames = openIdConnectProviderRegistry.getOpenIdConnectProviderNames();

			if (providerNames == null || providerNames.isEmpty()) {
				
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login Filter - Nenhum provedor OpenID Connect configurado.");
				}
				
				//Encaminha a requisicao e resposta para o proximo elemento no pipeline ou filtro
				chain.doFilter(servletRequest, servletResponse);
				
				return;
								
			}

			// Obtem o primeiro provedor disponivel
			String providerName = providerNames.iterator().next();

			HttpSession httpSession = request.getSession(false);
			String currentURL = OpenIdConnectFilterUtil.getRedirectParameter(request);
			
			// Cenario quando o usuario foi autenticado porem nao foi importado na base do liferay corretamente
			if (!currentURL.contains("StrangersNotAllowedException")) {
				
				// Cenario quando o usuario esta tentando fazer login direto no Liferay sem acessar urls de portlets
				if (currentURL.equals(PORTAL_LOGIN_URL)) {
										
		            URL url = new URL(portal.getHomeURL(request));
		        	String homeURL = url.toExternalForm();
					currentURL = homeURL;
					
				}

				// Armazena a URL atual na sessao
				if (httpSession != null) {
					httpSession.setAttribute(OpenIdConnectWebKeys.OPEN_ID_CONNECT_ACTION_URL, currentURL);
				}

				// Solicita autenticacao ao provedor
				openIdConnectServiceHandler.requestAuthentication(providerName, request, response);
				
			}
									
			if (log.isDebugEnabled()) {
				log.debug("OpenIdConnect Login Filter - Solicitando autenticacao para o provedor: " + providerName);
				log.debug("OpenIdConnect Login Filter - Encaminhado requisicao e resposta para o proximo filtro. URL de redirecionamento: " + currentURL);
			}
			
			//Encaminha a requisicao e resposta para o proximo elemento no pipeline ou filtro
			chain.doFilter(servletRequest, servletResponse);
			
		} catch (Exception e) {
			
			log.error(ERRO_OPENID_FILTER + e.getMessage(), e);
			
			// Interrompe o fluxo normal e nao encaminha o filtro
			return;
			
		} 
		
	}
	
}