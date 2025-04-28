Projeto para Integração de Login OpenID Connect para o Liferay
==============================

Esses módulos representam uma possível integração ao RHSSO de um determinado cliente no Liferay.

Este módulo, portanto, trabalha para fazer a autenticação do usuário via screenName como padrão, o redirecionamento para a tela do Login Provider caso o usuário não esteja autenticado e também foi implementado o processamento/importação de novos usuários para a base do Liferay utilizando também como critério o screenName.

Instalação
------------

Após a implantação dos módulos, os arquivos configs armazenados no módulo `openid-connect` devem ser copiados para `liferay.home/osgi/configs`:

* `com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectServiceHandlerImpl.config`

Tela administrativa do Liferay
---------

Para configurar isso, um novo provedor OpenID Connect deve ser configurado no Liferay. As informações devem ser fornecidas pelo mantenedor do RHSSO.

Novos provedores OpenID Connect são armazenados em Painel de Controle > Configuração > Configurações do Sistema > Segurança > SSO. Em geral, o OpenID Connect é usado aqui
ativado no item *OpenID Connect*. Uma nova entrada para RHSSO-PROVIDER-CONFIG pode ser armazenada em *Provider OpenID Connect*.

Este módulo suporta as seguintes entradas de escopo:
    
    openid

Versão do Liferay
-----------------

Este módulo foi testado com sucesso na seguinte versão do Liferay:

* Liferay CE 7.2.0 GA1

Fontes
-----------------------

Hintergrundinformationen zur Entwicklung können diesem Blog-Post entnommen werden: https://liferay.dev/blogs/-/blogs/integrating-verimi-with-liferay
Github - https://github.com/xdotgmbh/verimi-liferay-integration
OSGi (Open Service Gateway Initiative) - https://www.osgi.org/resources/where-to-start/
Liferay Module Projects - https://learn.liferay.com/w/dxp/liferay-development/liferay-internals/fundamentals/module-projects
