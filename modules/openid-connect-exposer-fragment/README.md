Módulo OpenID Connect Exposer
==============================

O Liferay, como qualquer aplicação OSGi, isola pacotes entre módulos. Classes marcadas como internal não são exportadas por padrão. Por isso, quando você tenta importar algo através do modulo openid-connect como:

`import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessorImpl;`

O OSGi tentará localizar o pacote com.liferay.portal.security.sso.openid.connect.internal em algum bundle que o exporte. Como o bundle original (.impl) não exporta esse pacote, o módulo openid-connect não consegue resolver a dependência, resultando no seguinte erro ao fazer o deployment:

`Unresolved requirement: Import-Package: com.liferay.portal.security.sso.openid.connect.internal`

O principal objetivo do openid-connect-exposer-fragment é “injetar” a exportação desses pacotes.

Detalhes:

 - Um Fragment Bundle nunca é ativado sozinho. Na tela administrativa do Liferay, o status de um fragment aparece como RESOLVED após o deployment bem-sucedido;
 - Sua principal função é ser anexado ao bundle host (com.liferay.portal.security.sso.openid.connect.impl) em tempo de execução;
 - Isso permite o compartilhamento do classloader com o bundle host, possibilitando o acesso a classes privadas, a sobrescrita de comportamentos, a exportação de pacotes ou até mesmo sua modificação — tudo respeitando as boas práticas do OSGi e do Liferay;
 - O deployment desse fragment .jar deve ocorrer antes do deployment do módulo openid-connect.
