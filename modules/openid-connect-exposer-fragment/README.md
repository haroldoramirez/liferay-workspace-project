Módulo OpenID Connect Exposer
==============================

O Liferay, como qualquer aplicação OSGi, isola pacotes entre módulos. Classes marcadas como internal não são exportadas por padrão. Por isso, quando você tenta importar algo como:

`import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessorImpl;`

O OSGi vai tentar encontrar o pacote com.liferay.portal.security.sso.openid.connect.internal em algum bundle que o exporte. Como o bundle original (.impl) não exporta esse pacote, o módulo opendi-connect não consegue resolver a dependência e gera o erro abaixo:

`Unresolved requirement: Import-Package: com.liferay.portal.security.sso.openid.connect.internal`

O principal objetivo do openid-connect-exposer-fragment é “injetar” a exportação desses pacotes no host que precisamos alterar.

Com isso, o pacote com.liferay.portal.security.sso.openid.connect.impl, passa a expor o que você precisa, e qualquer outro módulo (como seu hook ou customização) pode importa-lo normalmente.
